(ns clj.omnitech-styles.core
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]]))

(defstyles omnitech
  [:body
   {:font-family "sans-serif"
    :font-size (px 16)
    :line-height 1.5}])
