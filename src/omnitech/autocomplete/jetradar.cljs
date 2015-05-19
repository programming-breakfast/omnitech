(ns omnitech.autocomplete.jetradar
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.events :as gevents]
            [cljs.core.async :refer [put! <! chan]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [omnitech.autocomplete :as ac]
            [cljs.pprint :refer [pprint]])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))

(defn- suggestions-fn-generator [ac-endpoint]
  (fn [value suggestions-ch cancel-ch]
    (let [xhr (XhrIo.)]
      (gevents/listen xhr goog.net.EventType.SUCCESS
                      (fn [e]
                        (put! suggestions-ch (js->clj (.getResponseJson xhr) :keywordize-keys true))))
      (go
        (<! cancel-ch)
        (.abort xhr))
      (.send xhr (str ac-endpoint "?q=" value) "GET"))))

(defn container-view [_ _ _]
  (reify

    om/IDisplayName
    (display-name [_]
      "Jetradar Autocomplete Container")

    om/IRenderState
    (render-state [_ {:keys [input-component results-component]}]
      (dom/div #js {:className "ac-wrapper"}
               input-component results-component))))

(defn input-view [cursor _ {:keys [placeholder]}]
  (reify

    om/IDisplayName
    (display-name [_]
      "Jetradar Autocomplete Input")

    om/IRenderState
    (render-state [_ {:keys [focus-ch value-ch highlight-ch select-ch value highlighted-index result]}]
      (dom/div nil
               (dom/span nil (get-in cursor [:result :iata]))
               (dom/div #js {:className "ac-input-wrapper"}
                        (dom/input #js {:type "text"
                                        :autoComplete "off"
                                        :spellCheck "false"
                                        :className "ac-input"
                                        :placeholder placeholder
                                        :value (if-let [title (get-in cursor [:result :title])]
                                                 title
                                                 value)
                                        :onFocus (fn [e]
                                                   (put! focus-ch true)
                                                   (.preventDefault e))
                                        :onBlur (fn [e]
                                                  (put! focus-ch false)
                                                  (.preventDefault e))
                                        :onKeyDown (fn [e]
                                                     (case (.-keyCode e)
                                                       40 (put! highlight-ch (inc highlighted-index)) ;; up
                                                       38 (put! highlight-ch (dec highlighted-index)) ;; down
                                                       13 (put! select-ch highlighted-index) ;; enter
                                                       9  (put! select-ch highlighted-index) ;; tab
                                                       nil))
                                        :onChange #(put! value-ch (.. % -target -value))}))))))

(defn- results-view [app _ {:keys [loading-view loading-view-opts
                                   item-view item-view-opts]}]
  (reify

    om/IDisplayName
    (display-name [_]
      "Jetraar Autocomplete Results")

    om/IRenderState
    (render-state [_ {:keys [highlight-ch select-ch value loading? focused? suggestions highlighted-index]}]
      (let [display? (and focused? value (not= value ""))
            display (if display? "block" "none")
            attrs #js {:className "ac-items"
                       :style #js {:display display}}]
        (cond
          (and loading-view loading?)
          (dom/ul attrs
                  (om/build loading-view app {:opts loading-view-opts}))

          (not (empty? suggestions))
          (apply dom/ul attrs
                 (map-indexed
                  (fn [idx item]
                    (om/build item-view app {:init-state
                                             {:highlight-ch highlight-ch
                                              :select-ch select-ch}
                                             :state
                                             {:item item
                                              :index idx
                                              :highlighted-index highlighted-index}
                                             :opts item-view-opts}))
                  suggestions))

          :otherwise (dom/ul nil))))))

(defn- item-view [app owner {:keys [transform-fn]}]
  (reify

    om/IDisplayName
    (display-name [_]
      "Jetradar Autocomplete Item")

    om/IDidMount
    (did-mount [this]
      (let [{:keys [index highlight-ch select-ch]} (om/get-state owner)
            node (om/get-node owner)]
        (gevents/listen node (.-MOUSEOVER gevents/EventType) #(put! highlight-ch index))
        (gevents/listen node (.-CLICK gevents/EventType) #(put! select-ch index))))

    om/IRenderState
    (render-state [_ {:keys [item index highlighted-index]}]
      (let [highlighted? (= index highlighted-index)
            item (transform-fn item)]
        (dom/li #js {:className (if highlighted? "active ac-item" "ac-item")}
                (dom/div #js {:className "ac-item-container"}
                         (dom/span #js {:className "ac-item-iata"} (:iata item))
                         (dom/span #js {:className "ac-item-name"} (:name item))
                         (dom/span #js {:className "ac-item-country"} (:country item)))
                (dom/span #js {:className "ac-item-title"} (:title item)))))))

(defn jetradar-ac [app owner {:keys [ac-endpoint placeholder transform-fn]}]
  (reify

    om/IDisplayName
    (display-name [_]
      "Jetradar Autocomplete Component")

    om/IInitState
    (init-state [_]
      {:result-ch (chan)
       :suggestions-fn (suggestions-fn-generator ac-endpoint)
       :placeholder placeholder})

    om/IWillMount
    (will-mount [_]
      (let [result-ch (om/get-state owner :result-ch)]
        (go-loop []
          (let [[idx result] (<! result-ch)]
            (om/update! app :result (transform-fn result))
            (recur)))))

    om/IRenderState
    (render-state [_ {:keys [result-ch suggestions-fn placeholder]}]
      (om/build ac/autocomplete app
                {:opts
                 {:container-view container-view
                  :container-view-opts {}
                  :input-view input-view
                  :input-view-opts {:placeholder placeholder}
                  :results-view results-view
                  :results-view-opts {:item-view item-view
                                      :item-view-opts {:transform-fn transform-fn}}
                  :result-ch result-ch
                  :suggestions-fn suggestions-fn}}))))
