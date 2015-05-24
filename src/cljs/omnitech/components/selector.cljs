(ns cljs.omnitech.components.selector
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan]]))

(defn selector [cursor owner {:keys [result-ch
                                     container-view container-view-opts
                                     selected-view selected-view-opts
                                     dropdown-view dropdown-view-opts]}]
  (reify

    om/IDisplayName
    (display-name [_]
      "Core Selector component")

    om/IInitState
    (init-state [_]
      {:focus-ch (chan)
       :value-ch (chan)
       :focused? false})

    om/IWillMount
    (will-mount [_]
      (let [{:keys [focus-ch value-ch]} (om/get-state owner)]
        (go-loop []
          (alt!
            focus-ch ([v _] (om/set-state! owner :focused? v))
            value-ch ([v _] (om/set-state! owner :value v)))
          (recur))))

    om/IRenderState
    (render-state [_ {:keys [focus-ch value-ch value focused?]}]
      (om/build container-view cursor
                {:state
                 {:selected-component
                  (om/build selected-view cursor
                            {:init-state {:focus-ch focus-ch
                                          :value-ch value-ch}
                             :state {:value value
                                     :focused? focused?}
                             :opts selected-view-opts})
                  :dropdown-component
                  (om/build dropdown-view cursor
                            {:state {:value value
                                     :focused? focused?}
                             :opts dropdown-view-opts})}
                 :opts container-view-opts}))))

(defn passengers [cursor owner]
  (reify

    om/IInitState
    (init-state [_]
      {:focused? false})

    om/IRenderState
    (render-state [_ {:keys [focused?]}]
      (dom/div nil
              (dom/div #js {:onClick (fn [e]
                                      (om/set-state! owner :focused? (not focused?)))}
                      (str "Passengers: " (apply + (vals cursor))))
              (dom/ul #js {:style #js {:display (if focused? "block" "none")}}
                     (dom/li nil
                             (str "Adults " (:adults cursor))
                             (dom/button #js {:onClick (fn [e]
                                                         (om/transact! cursor :adults inc))}
                                         "+")
                             (dom/button #js {:onClick (fn [e]
                                                         (om/transact! cursor :adults dec))}
                                         "-"))
                     (dom/li nil
                             (str "Childs " (:childs cursor))
                             (dom/button #js {:onClick (fn [e]
                                                         (om/transact! cursor :childs inc))}
                                         "+")
                             (dom/button #js {:onClick (fn [e]
                                                         (om/transact! cursor :childs dec))}
                                         "-"))
                     (dom/li nil
                             (str "Infants " (:infants cursor))
                             (dom/button #js {:onClick (fn [e]
                                                         (om/transact! cursor :infants inc))}
                                         "+")
                             (dom/button #js {:onClick (fn [e]
                                                         (om/transact! cursor :infants dec))}
                                         "-")))))))
