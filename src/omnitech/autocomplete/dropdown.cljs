(ns omnitech.autocomplete.dropdown
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! chan put! timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as gevents]
            [omnitech.autocomplete :as ac]
            [cljs.pprint :refer [pprint]])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))

(defn container-view [_ _ {:keys [class-name]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [input-component results-component]}]
      (dom/div #js {:className (str "dropdown " class-name)}
               input-component results-component))))

(defn input-view [_ _ {:keys [class-name placeholder]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [focus-ch value-ch highlight-ch select-ch value highlighted-index]}]
      (dom/input #js {:type "text"
                      :autoComplete "off"
                      :spellCheck "false"
                      :className (str "form-control " class-name)
                      :placeholder placeholder
                      :value value
                      :onFocus (fn [e]
                                 (put! focus-ch true)
                                 (.preventDefault e))
                      :onBlur #(go (let [_ (<! (timeout 100))]
                                     ;; If we don't wait, then the dropdown will disappear before
                                     ;; its onClick renders and a selection won't be made.
                                     (put! focus-ch false)))
                      :onKeyDown (fn [e]
                                   (case (.-keyCode e)
                                     40 (put! highlight-ch (inc highlighted-index)) ;; up
                                     38 (put! highlight-ch (dec highlighted-index)) ;; down
                                     13 (put! select-ch highlighted-index) ;; enter
                                     9  (put! select-ch highlighted-index) ;; tab
                                     nil))
                      :onChange #(put! value-ch (.. % -target -value))}))))

(defn results-view [app _ {:keys [class-name
                                  loading-view loading-view-opts
                                  render-item render-item-opts]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [highlight-ch select-ch value loading? focused? suggestions highlighted-index]}]
      (let [display? (and focused? value (not= value ""))
            display (if display? "block" "none")
            attrs #js {:className (str "dropdown-menu " class-name)
                       :style #js {:display display}}]
        (cond
         (and loading-view loading?)
         (dom/ul attrs
                 (om/build loading-view app {:opts loading-view-opts}))

         (not (empty? suggestions))
         (apply dom/ul attrs
                (map-indexed
                 (fn [idx item]
                   (om/build render-item app {:init-state
                                              {:highlight-ch highlight-ch
                                               :select-ch select-ch}
                                              :state
                                              {:item item
                                               :index idx
                                               :highlighted-index highlighted-index}
                                              :opts render-item-opts}))
                 suggestions))

         :otherwise (dom/ul nil))))))

(defn render-item [app owner {:keys [class-name text-fn]}]
  (reify

    om/IDidMount
    (did-mount [this]
      (let [{:keys [index highlight-ch select-ch]} (om/get-state owner)
            node (om/get-node owner)]
        (gevents/listen node (.-MOUSEOVER gevents/EventType) #(put! highlight-ch index))
        (gevents/listen node (.-CLICK gevents/EventType) #(put! select-ch index))))

    om/IRenderState
    (render-state [_ {:keys [item index highlighted-index]}]
      (let [highlighted? (= index highlighted-index)]
        (dom/li #js {:className (if highlighted? (str "active " class-name) class-name)}
          (dom/a #js {:href "#" :onClick (fn [_] false)}
            (text-fn item index)))))))

(defn loading []
  (reify
    om/IRender
    (render [_]
      (dom/span nil " Loading..."))))

(defn suggestions-fn [value suggestions-ch cancel-ch]
  (let [xhr (XhrIo.)]
    (gevents/listen xhr goog.net.EventType.SUCCESS
                   (fn [e]
                     (put! suggestions-ch (js->clj (.getResponseJson xhr) :keywordize-keys true))))
    (go
      (<! cancel-ch)
      (.abort xhr))
    (.send xhr (str "http://www.jetradar.com/autocomplete/places?q=" value) "GET")))


(defn dropdown-ac [app owner]
  (reify
    om/IInitState
     (init-state [_]
       {:result-ch (chan)})

    om/IWillMount
    (will-mount [_]
      (let [result-ch (om/get-state owner :result-ch)]
        (go-loop []
          (let [[idx result] (<! result-ch)]
            (js/alert (str "Result is: " result))
            (recur)))))

    om/IRenderState
    (render-state [_ {:keys [result-ch]}]
      (om/build ac/autocomplete app
                {:opts
                 {:container-view container-view
                  :container-view-opts {}
                  :input-view input-view
                  :input-view-opts {:placeholder "Enter anything"}
                  :results-view results-view
                  :results-view-opts {:loading-view loading
                                      :render-item render-item
                                      :render-item-opts {:text-fn (fn [item _] (str item))}}
                  :result-ch result-ch
                  :suggestions-fn suggestions-fn}}))))
