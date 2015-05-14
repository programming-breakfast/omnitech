(ns ^:figwheel-always omnitech.core
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [cljs.pprint :refer [pprint]]
              [omnitech.autocomplete.simple :as simple-ac]
              [omnitech.autocomplete.dropdown :as dropdown-ac]
              [omnitech.autocomplete.jetradar :as jetradar-ac]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def app-state (atom {}))

;; (om/root simple-ac/simple-ac app-state {:target (js/document.getElementById "autocomplete-1")})

;; (om/root dropdown-ac/dropdown-ac app-state {:target (js/document.getElementById "autocomplete-2")})

(om/root jetradar-ac/jetradar-ac app-state {:target (js/document.getElementById "autocomplete-3")
                                            :opts {:ac-endpoint "http://www.jetradar.com/autocomplete/places"
                                                   :placeholder "Jetradar placeholder"}})

(defn pprint-app-state []
  (pprint @app-state))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc))
