(ns rest-api.classes.variation.widgets.isolation
  (:require
    [rest-api.classes.generic-fields :as generic]
    [pseudoace.utils :as pace-utils]
    [rest-api.formatters.object :as obj :refer [pack-obj]]))

(defn transposon-excision [variation]
  {:data nil; (some->> (:variation/transposon-excision variation))
   :description "was the variation generated by a transposon excision event, and if so, of which family?"})

(defn derivative [variation]
  {:data (some->> (:variation/_derived-from-variation variation)
                  (map pack-obj))
   :description "variations derived from this variation"})

(defn derived-from [variation]
  {:data (some->> (:variation/derived-from-variation variation)
                  (first)
                  (pack-obj))
   :description "variation from which this one was derived"})

(defn external-source [variation]
  {:data nil
   :description "dbSNP ss#, if known"})

(defn isolated_via_reverse_genetics [variation]
  {:data nil ; :variation/reverse-genetics
   :description "was the mutation isolated by reverse genetics?"})

(defn transposon-insertion [variation]
  {:data nil ; :variation/transposon-insertion
   :description "was the variation generated by a transposon insertion event, and if so, of which family?"})

(defn isolated-by [variation]
  {:data (some->> (:variation/person variation)
                  (first)
                  (pack-obj))
   :description "the person credited with generating the mutation"})

(defn mutagen [variation]
  {:data nil
   :description "mutagen used to generate the variation"})

(defn isolated-by-author [variation]
  {:data (some->> (:variation/author variation)
                  (first)
                  (pack-obj))
   :description "the author credited with generating the mutation"})

(defn isolated-via-forward-genetics [variation]
  {:data nil ; variation/foward-genetics
   :description "was the mutation isolated by forward genetics?"})

(defn date-isolated [variation]
  {:data (:variation/date variation)
   :description "date the mutation was isolated"})

(def widget
  {:name  generic/name-field
   :laboratory generic/laboratory
   :transposon_excision transposon-excision
   :derivative derivative
   :derived_from derived-from
   :external_source external-source
   :isolated-via-reverse-genetics isolated_via_reverse_genetics
   :transposon_insertion transposon-insertion
   :isolated_by isolated-by
   :mutagen mutagen
   :isolated_by_author isolated-by-author
   :isolated_via_forward_genetics isolated-via-forward-genetics
   :date_isolated date-isolated})
