(ns rest-api.classes.gene.widgets.location
  (:require
    [rest-api.classes.sequence.main :as sequence-fns]
    [rest-api.classes.generic-fields :as generic]))

(defn genetic-position [gene]
  (let [segment (sequence-fns/get-longest-segment gene)
        gene-map (:gene/map gene)
        chr (:map/id (:gene.map/map gene-map))
        map-position (:map-position/position gene-map)
        error (:map-error/error map-position)
        position (:map-position.position/float map-position)]
    {:data [{:chromosome chr
             :position position
             :error error
             :formatted (if (nil? error)
                          (format "%s:%2.2f +/- (unknown)f cM" chr position)
                          (format "%s:%2.2f +/- %2.3f cM" chr position error))
             :method ""}]
     :description (str "Genetic position of Gene:" (:gene/id gene))}))

(defn tracks [gene]
  {:data (if (:gene/corresponding-transposon gene)
           ["TRANSPOSONS"
            "TRANSPOSON_GENES"]
           ["GENES"
            "VARIATIONS_CLASSICAL_ALLELES"
            "CLONES"])
   :description "tracks displayed in GBrowse"})

(defn genomic-image [gene]
  {:data (sequence-fns/genomic-obj gene)
   :description "The genomic location of the sequence to be displayed by GBrowse"})

(def widget
    {:name generic/name-field
     :genetic_position genetic-position
     :tracks tracks
     :genomic_position generic/genomic-position
     :genomic_image genomic-image})
