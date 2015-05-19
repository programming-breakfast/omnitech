(ns omnitech.segment
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [omnitech.autocomplete.jetradar :refer [jetradar-ac]]))

(defn- transform-fn [{:keys [code title city_name country_name type]}]
  {:iata code
   :name city_name
   :country country_name
   :title title
   :type type})

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
               (om/build jetradar-ac (:destination cursor) {:opts {:ac-endpoint ac-endpoint
                                                                   :placeholder "Select destination"
                                                                   :transform-fn transform-fn}})))))
