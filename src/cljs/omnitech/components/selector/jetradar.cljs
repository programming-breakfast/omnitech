(ns cljs.omnitech.components.selector.jetradar
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! <! chan]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.omnitech.components.selector :as s]))

(defn container-view [_ _ _]
  (reify

    om/IDisplayName
    (display-name [_]
      "Jetradar Selector Container")

    om/IRenderState
    (render-state [_ {:keys [selected-component dropdown-component]}]
      (dom/div #js {:className "selector-wrapper"}
               selected-component dropdown-component))))

(defn selected-passengers-view [cursor _ _]
  (reify

    om/IDisplayName
    (display-name [_]
      "Jetradar Selector Selected Passengers")

    om/IRenderState
    (render-state [_ {:keys [focus-ch value-ch value passengers focused?]}]
      (dom/div #js {:className "passengers"
                    :onClick (fn [e]
                               (put! focus-ch (not focused?))
                               (.preventDefault e))}
               (if-let [passengers-values (vals passengers)]
                 (str "Passengers: " (apply + (vals passengers)))
                 "Passengers: 1 adult")))))

(defn passenger-controls [cursor owner {:keys [passenger-type result-ch]}]
  (reify

    om/IRender
    (render [_]
      (dom/span nil
                (dom/button #js {:onClick (fn [e]
                                            (put! result-ch {passenger-type inc})
                                            (.preventDefault e))})
                (dom/button #js {:onClick (fn [e]
                                            (om/transact! cursor [passenger-type] dec)
                                            (.preventDefault e))})))))

(defn dropdown-passengers-view [cursor _ {:keys [result-ch]}]
  (reify

    om/IDisplayName
    (display-name [_]
      "Jetradar Selector Controls Passengers")

    om/IRenderState
    (render-state [_ {:keys [focused?]}]
      (let [display? focused?
            display (if focused? "block" "none")
            attrs #js {:className "selector-controls"
                       :style #js {:display display}}]
        (dom/ul attrs
                (dom/li nil
                        (om/build passenger-controls cursor {:opts {:passenger-type :adult
                                                                    :result-ch result-ch}})))))))

(defn jetradar-passengers-s [app owner {:keys [current-passengers]}]
  (reify

    om/IDisplayName
    (display-name [_]
      "Jetradar Selector Passengers")

    om/IInitState
    (init-state [_]
      {:result-ch (chan)
       :passengers current-passengers})

    om/IRenderState
    (render-state [_ {:keys [result-ch passengers]}]
      (om/build s/selector app
                {:opts
                 {:result-ch result-ch
                  :container-view container-view
                  :container-view-opts {}
                  :selected-view selected-passengers-view
                  :selected-view-opts {}
                  :dropdown-view dropdown-passengers-view
                  :dropdown-view-opts {:result-ch result-ch}}}))))
