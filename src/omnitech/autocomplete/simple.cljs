(ns omnitech.autocomplete.simple
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [omnitech.autocomplete :as ac]
            [cljs.core.async :refer [<! chan put!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [cljs.pprint :refer [pprint]])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))

(defn suggestions-fn [value suggestions-ch cancel-ch]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.SUCCESS
                   (fn [e]
                     (put! suggestions-ch (js->clj (.getResponseJson xhr) :keywordize-keys true))))
    (go
      (<! cancel-ch)
      (.abort xhr))
    (.send xhr (str "http://www.jetradar.com/autocomplete/places?q=" value) "GET")))

(defn container-view [_ _]
  (reify
    om/IRenderState
    (render-state [_ {:keys [input-component results-component]}]
      (dom/div nil input-component results-component))))

(defn input-view [_ _]
  (reify
    om/IRenderState
    (render-state [_ {:keys [value-ch value]}]
      (dom/input
       #js {:type "text"
            :autoComplete "off"
            :spellCheck "false"
            :value value
            :onChange #(put! value-ch (.. % -target -value))}))))

(defn render-empty-suggestion []
  (dom/div #js {:className "suggestion"}
           (dom/div #js {:className "empty-suggestion"} "No results!")))

(defn render-suggestion [{:keys [title code]}]
  (dom/div #js {:className "suggestion"}
           (dom/div #js {:className "suggestion-title"} title)
           (dom/div #js {:className "suggestion-code"} code)))

(defn results-view [_ _]
  (reify
    om/IRenderState
    (render-state [_ {:keys [suggestions]}]
      (apply dom/div nil
             (if suggestions
               (map render-suggestion (take 10 suggestions))
               (repeatedly 1 render-empty-suggestion))))))

(defn simple-ac [app owner]
  (reify
    om/IRenderState
    (render-state [_ _]
      (om/build ac/autocomplete app
                {:opts
                 {:container-view container-view
                  :input-view input-view
                  :results-view results-view
                  :suggestions-fn suggestions-fn}}))))

