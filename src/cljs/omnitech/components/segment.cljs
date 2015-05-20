(ns cljs.omnitech.components.segment
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.omnitech.components.autocomplete.jetradar :refer [jetradar-ac]]))

(defn- transform-fn [{:keys [code title city_name country_name type]}]
  {:iata code
   :name city_name
   :country country_name
   :title title
   :type type})

(defn- swap-origin-and-destination [cursor]
  (let [orig (get cursor :origin)
        dest (get cursor :destination)]
    ;; TODO: Add check
    (om/update! cursor :origin dest)
    (om/update! cursor :destination orig)))

(defn swap-destinations [cursor owner]
  (reify

    om/IDisplayName
    (display-name [_]
      "Switcher for origin and destination")

    om/IRender
    (render [_]
      (dom/button #js {:onClick (fn [_]
                                  (swap-origin-and-destination cursor))}
                  "Change origin and destination"))))

(defn segment [cursor owner {:keys [ac-endpoint]}]
  (reify

    om/IDisplayName
    (display-name [_]
      "Segment Component")

    om/IRender
    (render [_]
      (dom/div #js {:className "segment"
                    :style #js {:display (if (:editable? cursor) "block" "none")}}
               (om/build jetradar-ac (:origin cursor) {:opts {:ac-endpoint ac-endpoint
                                                              :placeholder "Select origin"
                                                              :transform-fn transform-fn}})
               (om/build swap-destinations cursor)
               (om/build jetradar-ac (:destination cursor) {:opts {:ac-endpoint ac-endpoint
                                                                   :placeholder "Select destination"
                                                                   :transform-fn transform-fn}})))))
