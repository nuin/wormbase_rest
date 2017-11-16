(ns rest-api.classes.construct.widgets.transgene
  (:require
   [rest-api.classes.generic-fields :as generic]
   [rest-api.formatters.object :as obj :refer [pack-obj]]))

(defn transgenes [c]
  {:data (some->> (:construct/transgene-construct c)
                  (map (fn [o]
                         {:transgene (pack-obj o)
                          :summary (:transgene.summary/text (:transgene/summary o))
                          :strain (some->> (:transgene/strain o)
                                           (map pack-obj)
                                           (sort-by :label))
                          :reference (some->> (:transgene/reference o)
                                              (map :transgene.reference/paper)
                                              (map pack-obj)
                                              (sort-by :label))})))
   :description "Transgenes generated by this construct"})

(def widget
  {:transgenes transgenes
   :name generic/name-field})
