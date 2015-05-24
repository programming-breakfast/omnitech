(ns ^:figwheel-always cljs.omnitech.core
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [cljs.pprint :refer [pprint]]
              [cljs.omnitech.components.segment :refer [segment]]
              [cljs.omnitech.components.selector.jetradar :refer [jetradar-passengers-s]]
              [cljs.omnitech.components.selector :refer [passengers]]
              [cljs-time.core :as time]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def app-state (atom {:segments [{:origin {}
                                  :destination {}
                                  :date (time/now)
                                  :editable? true}
                                 {:origin {}
                                  :destination {}
                                  :date (time/plus (time/now) (time/weeks 2))
                                  :editable? false}]
                      :oneway false
                      :passengers {:adults 1
                                   :childs 0
                                   :infants 0
                                   }}))

(def passengers-state {:passengers {:adults 1
                                   :childs 0
                                   :infants 0
                                   }})

(defn oneway-roundtrip-button [cursor owner]
  (reify

    om/IDisplayName
    (display-name [_]
      "Trip Type Switcher")

    om/IRender
    (render [_]
      (dom/button #js {:onClick (fn [e]
                                  (om/transact! cursor :oneway (fn [_] (not (:oneway cursor)))))}
                  (if (:oneway cursor)
                    "->"
                    "<->")))))

(defn application [cursor owner]
  (reify

    om/IDisplayName
    (display-name [_]
      "Main Application")

    om/IRender
    (render [_]
      (dom/div nil
               (apply dom/div nil
                      (om/build-all segment (:segments cursor) {:opts
                                                                {:ac-endpoint "http://www.jetradar.com/autocomplete/places"}}))
               (om/build oneway-roundtrip-button cursor)
               ;; TODO: place for passengers selector
               (om/build passengers (:passengers cursor))
               ))))

(om/root application
         app-state
         {:target (js/document.getElementById "app")})

(defn pprint-app-state []
  (pprint @app-state))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc))
