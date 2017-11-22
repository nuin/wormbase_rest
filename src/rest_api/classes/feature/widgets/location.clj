(ns rest-api.classes.feature.widgets.location
  (:require
    [rest-api.classes.sequence.core :as sequence-fns]
    [rest-api.classes.generic-fields :as generic]))

(defn tracks [feature]
  {:data ["GENES"
          "RNASEQ_ASYMMETRIES"
          "RNASEQ"
          "RNASEQ_SPLICE"
          "POLYSOMES"
          "MICRO_ORF"
          "DNASEI_HYPERSENSITIVE_SITE"
          "REGULATORY_REGIONS"
          "PROMOTER_REGIONS"
          "HISTONE_BINDING_SITES"
          "TRANSCRIPTION_FACTOR_BINDING_REGION"
          "TRANSCRIPTION_FACTOR_BINDING_SITE"
          "GENOME_SEQUENCE_ERROR_CORRECTED"
          "BINDING_SITES_PREDICTED"
          "BINDING_SITES_CURATED"
          "BINDING_REGIONS"
          "GENOME_SEQUENCE_ERROR"]
   :description "tracks displayed in GBrowse"})

(defn genomic-image [feature]
  {:data (sequence-fns/genomic-obj feature)
   :description "The genomic location of the sequence to be displayed by GBrowse"})

(def widget
    {:name generic/name-field
     :genetic_position generic/genetic-position
     :tracks tracks
     :genomic_position generic/genomic-position
     :genomic_image genomic-image})
