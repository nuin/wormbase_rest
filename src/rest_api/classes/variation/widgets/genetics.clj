(ns rest-api.classes.variation.widgets.genetics
  (:require
    [datomic.api :as d]
    [rest-api.classes.generic :as global-generic]
    [rest-api.classes.variation.generic :as generic]
    [rest-api.classes.gene.widgets.genetics :as gene-genetics]
    [rest-api.classes.gene.variation :as gene-variation]
    [rest-api.formatters.object :as obj :refer  [pack-obj]]))

(defn- get-gene-from-variation [variation]
  (seq (map :variation.gene/gene (:variation/gene variation))))

(defn gene-class [variation]
  {:data (if-let [gene-class (:gene-class variation)]
             (map (pack-obj) gene-class))
   :description "the class of the gene the variation falls in, if any"})

(defn corresponding-gene [variation]
  {:data (if-let [gene (:corresponding-transgene variation)]
             (map (pack-obj) gene))
   :description "gene in which this variation is found (if any)"})

(defn reference-allele [variation]
  (let [genes []; (get-gene-from-variation variation)
        gene (first genes)]
  (gene-genetics/reference-allele gene)))

; Working - not sure if right though
(defn other-allele [variation]
  {:data (let [genes (map :variation.gene/gene (:variation/gene variation))]
           (for [gene genes]
             (let [db  (d/entity-db gene)
                   alleles (d/q '[:find [?var ...]
                                  :in $ ?variation ?gene
                                  :where [?vh :variation.gene/gene ?gene]
                                         [?var :variation/gene ?vh]
                                         [?var :variation/allele true]
                                         (not [?var :variation/phenotype _])
                                         [(not= ?var ?variation)]]
                                db (:db/id variation) (:db/id gene))
                   polymorphisms (filter
                                   (fn [aid]
                                     (let [allele (d/entity db aid)]
                                       (contains? allele :variation/confirmed-snp))) alleles)
                   sequenced-alleles (filter
                                       (fn [aid]
                                         (let [allele (d/entity db aid)]
                                           (not
                                             (contains? allele :variation/confirmed-snp)))) alleles)]
               {:polymorphisms (for [polymorphism polymorphisms]
                                 (pack-obj (d/entity db polymorphism)))
                :sequenced_alleles (for [sequenced-allele sequenced-alleles]
                                     (pack-obj (d/entity db  sequenced-allele)))})))
   :description "other variations of the containing gene (if known)"})

; need to varify
(defn linked-to [variation]
  {:data (if-let [links (:variation/linked-to variation)]
           (for [linked-to links] (pack-obj linked-to)))
  :description "linked_to"})

(defn strain [variation]
  {:data (if-let [strains (map :variation.strain/strain (:variation/strain variation))]
           (global-generic/categorize-strains strains))
   :description "strains carrying this variation"})

(defn rescued-by-transgene [variation]
  {:data nil
   :description "transgenes that rescue phenotype(s) caused by this variation"})

(def widget
  {:name                 generic/name-field
   :gene_class           gene-class
   :corresponding_gene   corresponding-gene
   :reference_allele     reference-allele
   :other_allele         other-allele
   :linked_to            linked-to
   :strain               strain
   :rescued_by_transgene rescued-by-transgene})
