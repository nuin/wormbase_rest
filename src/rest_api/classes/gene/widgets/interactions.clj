(ns rest-api.classes.gene.widgets.interactions
  (:require
   [clojure.string :as str]
   [datomic.api :as d]
   [pseudoace.utils :as pace-utils]
   [rest-api.classes.generic-fields :as generic]
   [rest-api.formatters.object :refer [pack-obj]]))

(def ^:private sort-by-id (partial sort-by :id))

(def ^:private interaction-phenotype-kw
  :interaction/interaction-phenotype)

(def ^:private does-not-regulate-kw
  :interaction.regulation-result.value/does-not-regulate)

(def ^:private interactor-role-map
  {:interactor-info.interactor-type/affected :affected
   :interactor-info.interactor-type/bait :affected
   :interactor-info.interactor-type/cis-regulated :affected
   :interactor-info.interactor-type/cis-regulator :effector
   :interactor-info.interactor-type/effector :effector
   :interactor-info.interactor-type/target :effector
   :interactor-info.interactor-type/trans-regulated :affected
   :interactor-info.interactor-type/trans-regulator :effector})

(def ^:private interaction-target
  (some-fn :interaction.feature-interactor/feature
           :interaction.interactor-overlapping-gene/gene
           :interaction.molecule-interactor/molecule
           :interaction.rearrangement/rearrangement))

(def ^:private interactors
  [:interaction/feature-interactor
   :interaction/interactor-overlapping-cds
   :interaction/interactor-overlapping-gene
   :interaction/interactor-overlapping-protein
   :interaction/molecule-interactor])

(def ^:private corresponding-cds
  (some-fn
   (comp :gene/_corresponding-cds :gene.corresponding-cds/_cds)))

(def interactor
  (some-fn :molecule/id :gene/id :rearrangement/id :feature/id))

(def int-rules
  '[[(gene->interaction-3 ?gene ?ih ?int)
     [?ih :interaction.interactor-overlapping-gene/gene ?gene]
     [?int :interaction/interactor-overlapping-gene ?ih]]
    [(gene-interaction ?gene ?int)
     (gene->interaction-3 ?gene _ ?int)]
    [(interaction->gene-3 ?int ?ih ?gene)
     [?int :interaction/interactor-overlapping-gene ?ih]
     [?ih :interaction.interactor-overlapping-gene/gene ?gene]]
    [(gene->neighbour-5 ?gene ?gh ?neighbour ?nh ?ix)
     (gene->interaction-3 ?gene ?gh ?ix)
     (interaction->gene-3 ?ix ?nh ?neighbour)
     [(not= ?gene ?neighbour)]]
    [(gene->neighbour-5-non-predicted ?gene ?gh ?neighbour ?nh ?ix)
     (gene->neighbour-5 ?gene ?gh ?neighbour ?nh ?ix)
     (not
      [?ix :interaction/type :interaction.type/predicted])]
    [(gene-neighbour ?gene ?neighbour)
     (gene->neighbour-5 ?gene _ ?neighbour _ _)]]
  )

(defn interactor-idents [db]
  (sort (d/q '[:find [?ident ...]
               :where
               [?e :db/ident ?ident]
               [?e :pace/use-ns "interactor-info"]
               [_ :db/valueType :db.type/ref]
               [_ :db.install/attribute ?e]
               [(namespace ?ident) ?ns]
               [(= ?ns "interaction")]]
             db)))

;; Schema doesn't change once a process is running
(def interactor-refs (memoize interactor-idents))

(defn- interactor-role [interactor]
  (let [int-types (:interactor-info/interactor-type interactor)]
    (or (interactor-role-map (first int-types))
        (if (corresponding-cds (interaction-target interactor))
          :associated-product)
        :other)))

(defn- regulatory-result [interaction]
  (some->> (:interaction/regulation-result interaction)
           (map :interaction.regulation-result/value)
           first))

(defn- humanize-name [ident]
  (let [ident-name (name ident)
        hname (-> (name ident)
                  (str/split #":")
                  (last)
                  (str/replace #"-" " ")
                  (str/capitalize))]
    ;; hacks for when hname trick isn't good enough
    (cond
      (= hname "Proteinprotein") "ProteinProtein"
      :default hname)))

(def deprecated-interaction-types
  (set [:interaction.type/genetic:asynthetic
        :interaction.type/genetic:complete-mutual-suppression
        :interaction.type/genetic:complete-suppression
        :interaction.type/genetic:complete-unilateral-suppression
        :interaction.type/genetic:enhancement
        :interaction.type/genetic:epistasis
        :interaction.type/genetic:maximal-epistasis
        :interaction.type/genetic:minimal-epistasis
        :interaction.type/genetic:mutual-enhancement
        :interaction.type/genetic:mutual-oversuppression
        :interaction.type/genetic:mutual-suppression
        :interaction.type/genetic:negative-genetic
        :interaction.type/genetic:neutral-epistasis
        :interaction.type/genetic:neutral-genetic
        :interaction.type/genetic:no-interaction
        :interaction.type/genetic:opposing-epistasis
        :interaction.type/genetic:oversuppression
        :interaction.type/genetic:oversuppression-enhancement
        :interaction.type/genetic:partial-mutual-suppression
        :interaction.type/genetic:partial-suppression
        :interaction.type/genetic:partial-unilateral-suppression
        :interaction.type/genetic:phenotype-bias
        :interaction.type/genetic:positive-epistasis
        :interaction.type/genetic:positive-genetic
        :interaction.type/genetic:qualitative-epistasis
        :interaction.type/genetic:quantitative-epistasis
        :interaction.type/genetic:suppression
        :interaction.type/genetic:suppression-enhancement
        :interaction.type/genetic:synthetic
        :interaction.type/genetic:unilateral-enhancement
        :interaction.type/genetic:unilateral-oversuppression
        :interaction.type/genetic:unilateral-suppression]))

(defn- interaction-type-name [interaction]
  (let [itypes (->> (:interaction/type interaction)
                    (remove #(deprecated-interaction-types %))
                    (map name))
        type-name (or (some #(or (re-matches #"^gi-module-three:neutral" %)
                                 (re-matches #"^gi-module-two.*" %)
                                 (re-matches #"^gi-module-three.*" %))
                            itypes)
                      (first itypes))]
    (case type-name
      "physical:proteindna" "physical:protein-DNA"
      "physical:proteinprotein" "physical:protein-protein"
      "physical:proteinrna" "physical:protein-RNA"

      (cond
       ;; Hack to produce the "type-name" when real type-name
       ;; is regulatory.
       ;; TODO: Perhaps the proposed module system should be able to address
       ;;       this and produce a cleaner solution.
       (re-matches #"^regulatory.*" type-name)
       (let [reg-res (some-> (regulatory-result interaction)
                             (name))
             subtype (case reg-res
                       "negative-regulate" "negatively regulates"
                       "positive-regulate" "positively regulates"
                       "does-not-regulate" "does not regulate"
                       "other")]
         (format "regulatory:%s" subtype))

       (or (re-matches #"^genetic.*" type-name)
           (re-matches #"^gi-module-one.*" type-name))
       "genetic:other"

       (and (not= "predicted" type-name)
            (not (re-matches #".+:.+" type-name)))
       (format "%s:other" type-name)

       :default
       type-name))))

(defn gene-direct-interactions [db gene]
  (d/q '[:find ?int ?gh ?nh
         :in $ % ?gene
         :where
         (gene->neighbour-5 ?gene ?gh _ ?nh ?int)]
       db int-rules gene))

(defn gene-nearby-interactions [db gene]
  (->> (d/q '[:find ?int ?nh ?n2h
              :in $ % ?gene
              :where
              (gene->neighbour-5-non-predicted ?gene _ ?neighbour1 _ ?i1)
              (gene->neighbour-5-non-predicted ?gene _ ?neighbour2 _ ?i2)
              (gene->neighbour-5-non-predicted ?neighbour1 ?nh ?neighbour2 ?n2h ?int)]
            db int-rules gene)))

;; (defn gene-nearby-interactions [db gene]
;;   (->> (d/q '[:find ?int (count ?ng)
;;               :in $ % ?gene
;;               :where
;;               (gene-neighbour ?gene ?ng)
;;               (gene-interaction ?ng ?int)]
;;             db int-rules gene)
;;        (filter (fn [[_ cnt]]
;;                  (> cnt 1)))
;;        (map first)))

;; (defn gene-neighbours? [interaction gene-1 gene-2]
;;   (let [db (d/entity-db gene-1)
;;         [ix-id g1-id g2-id] (map :db/id [interaction gene-1 gene-2])]
;;     (when (and g1-id g2-id)
;;       (d/q '[:find (count ?ng) .
;;              :in $ % ?int ?gene ?ng
;;              :where
;;              (gene-neighbour ?gene ?ng)
;;              (gene-interaction ?ng ?int)]
;;              db
;;              int-rules
;;              ix-id
;;              g1-id
;;              g2-id))))

(defn- predicted [int-type role data]
  (if (= int-type "Predicted")
    (if-let [node-predicted (get-in data [:nodes (:id role) :predicted])]
      node-predicted
      1)
    0))

(defn gene-interaction-tuples [obj nearby?]
  (let [db (d/entity-db obj)
        id (:db/id obj)]
    (concat
     (gene-direct-interactions db id)
     (if nearby?
       (gene-nearby-interactions db id)))))

(defn- entity-ident
  ([role]
   (entity-ident role :class))
  ([role role-selector]
   (keyword (role-selector role) "id")))

(defn- annotate-role [data int-type role]
  (let [ident (entity-ident role)]
    (pace-utils/vassoc role
                       :predicted (predicted int-type role data))))

(defn- assoc-interaction [type-name nearby? data unpacked]
  (let [key-path [:nodes (:id unpacked)]
        ar (annotate-role data type-name unpacked)]
    (if (and (nil? (get-in data key-path)) ar)
      (-> data
          (assoc-in key-path ar)
          (assoc-in [:ntypes (:class unpacked)] 1))
      data)))

(defn- update-in-uniq [data path func value]
  (update-in data
             path
             (fn [old new]
               (->> (func old new)
                    (set)
                    (sort-by-id)
                    (vec)))
             value))

(defn- update-in-edges
  "Updates interaction `packed-int` and `papers` to a unique
  collection within the edges data structure."
  [data int-key packed-int papers]
  (-> data
      (update-in-uniq [:edges int-key :interactions]
                      conj
                      packed-int)
      (update-in-uniq [:edges int-key :citations]
                      into
                      papers)))

(defn- fixup-citations [edges]
  (map (fn [edge]
         (let [citations (:citations edge)
               interactions (:interactions edge)
               n-interactions (count interactions)
               citations* (if (and (> n-interactions 1)
                                   (= (count citations) 1))
                            (->> (first citations)
                                 (repeat n-interactions)
                                 (vec))
                            citations)]
           (merge edge {:citations citations*})))
       edges))

(defn- entity-type [entity-map]
  (some->> (keys entity-map)
           (filter #(= (name %) "id"))
           (first)
           (namespace)))

(defn- involves-focus-gene? [focus-gene interactors nearby?]
  (let [ident-vals (map :gene/id interactors)
        ids (->> ident-vals (remove nil?) vec)
        focused? (some #{(:gene/id focus-gene)} ids)]
    (when focused?
      ids)))

(defn- overlapping-genes [interaction]
   (->> (:interaction/interactor-overlapping-gene interaction)
        (map :interaction.interactor-overlapping-gene/gene)))

(defn- pack-int-roles
  "Pack interaction roles into the expected format."
  [interaction nearby? a b direction]
  (let [type-name (interaction-type-name interaction)
        non-directional? (= direction "non-directional")]
    (for [x a
          y b]
        (let [xt (interaction-target x)
              yt (interaction-target y)
              participants (if non-directional?
                             (vec (sort-by-id [xt yt]))
                             [xt yt])
              packed (map pack-obj participants)
              roles (zipmap [:effector :affected] participants)
              labels (filter identity (map :label packed))
              ;; ix-gene-neigbours? (partial gene-neighbours? interaction ref-obj)
              ;; participant-ids (involves-focus-gene? ref-obj participants nearby?)
              ;; includes-focus-gene? (seq participant-ids)
              ]
          (cond
            ;; (= xt yt) nil

            ;; ;; Indirect interactions (= nearby? true)
            ;; (and nearby?
            ;;      (some nil? (map ix-gene-neigbours? [xt yt])))
            ;;  nil

            ;; Direct interactions  (= nearby? false)
            ;; (and (not nearby?)
            ;;      (not= (entity-type ref-obj) "interaction")
            ;;      (not includes-focus-gene?))
            ;; nil
            :default
            (let [result-key (str/trim (str/join " " labels))]
              (when result-key
                (let [result (merge {:type-name type-name
                                     :direction direction} roles)]
                  [result-key result]))))))))

(defn- interaction-info [ia holder1 holder2 nearby?]
  (let [possible-int-types (get ia :interaction/type #{})
          no-interaction :interaction.type/genetic:no-interaction
          lls (or (:interaction/log-likelihood-score ia) 1000)]
      (cond
       (and (<= lls 1.5)
            (possible-int-types :interaction.type/predicted))
       nil

       :default
       (let [{effectors :effector
              affected :affected
              others :other
              associated :associated-product} (group-by interactor-role
                                                        [holder1 holder2])
              pack-iroles (partial pack-int-roles ia nearby?)
              roles (if (and effectors affected)
                      (pack-iroles effectors affected "Effector->Affected")
                      (pack-iroles [holder1] [holder2] "non-directional"))]
         (when (seq (remove nil? roles))
           (->> roles
                (vec)
                (into {})
                (vals))))
       )))

(defn- annotate-interactor-roles [data type-name int-roles]
  (->> int-roles
       (map pack-obj)
       (map (partial annotate-role data type-name))))

(defn- pack-papers [papers]
  (->> papers
       (map (partial pack-obj "paper"))
       (vec)))

(defn- assoc-showall [data nearby?]
  (assoc data :showall (or (< (count (:edges data)) 100) nearby?)))

(defn- edge-key [x y type-name direction phenotype]
  (str/trimr
   (str x " " y " " type-name " " direction " " (:label phenotype))))



(defn- process-obj-interaction
  [nearby? data interaction type-name effector affected direction]
  (let [roles [effector affected]
        packed-roles (annotate-interactor-roles data type-name roles)
        [packed-effector packed-affected] packed-roles
        [e-name a-name] (map :id packed-roles)
        papers (:interaction/paper interaction)
        packed-papers (pack-papers papers)
        phenotype (first (interaction-phenotype-kw interaction))
        packed-int (pack-obj "interaction" interaction)
        packed-phenotype (pack-obj "phenotype" phenotype)
        e-key (edge-key e-name a-name type-name direction packed-phenotype)
        a-key (edge-key a-name e-name type-name direction packed-phenotype)
        assoc-int (partial assoc-interaction type-name nearby?)
        result (-> data
                   (assoc-in [:types type-name] 1)
                   (assoc-int packed-effector)
                   (assoc-int packed-affected))]
     (let [result* (cond
                      (get-in result [:edges e-key])
                      (update-in-edges result e-key packed-int packed-papers)

                      (and (= direction "non-directional")
                           (get-in result [:edges a-key]))
                      (update-in-edges result a-key packed-int packed-papers)

                      :default
                      (assoc-in result
                                [:edges e-key]
                                {:affected packed-affected
                                 :citations packed-papers
                                 :direction direction
                                 :effector packed-effector
                                 :interactions [packed-int]
                                 :phenotype packed-phenotype
                                 :type type-name
                                 :nearby (if nearby? "1" "0")}))]
       (assoc-showall result* nearby?))))

;; (defn- process-obj-interaction
;;   [nearby? data interaction type-name effector affected direction]
;;   (let [roles [effector affected]
;;         packed-roles (annotate-interactor-roles data type-name roles)
;;         [packed-effector packed-affected] packed-roles
;;         [e-name a-name] (map :label packed-roles)
;;         papers (:interaction/paper interaction)
;;         packed-papers (pack-papers papers)
;;         phenotype (first (interaction-phenotype-kw interaction))
;;         packed-int (pack-obj "interaction" interaction)
;;         packed-phenotype (pack-obj "phenotype" phenotype)
;;         e-key (edge-key e-name a-name type-name packed-phenotype)
;;         a-key (edge-key a-name e-name type-name packed-phenotype)
;;         assoc-int (partial assoc-interaction type-name nearby?)
;;         result (-> data
;;                    (assoc-in [:types type-name] 1)
;;                    (assoc-int packed-effector)
;;                    (assoc-int packed-affected))]
;;      (let [result* (cond
;;                       (get-in result [:edges e-key])
;;                       (update-in-edges result e-key packed-int packed-papers)

;;                       (get-in result [:edges a-key])
;;                       (update-in-edges result a-key packed-int packed-papers)

;;                       :default
;;                       (assoc-in result
;;                                 [:edges e-key]
;;                                 {:affected packed-affected
;;                                  :citations packed-papers
;;                                  :direction direction
;;                                  :effector packed-effector
;;                                  :interactions [packed-int]
;;                                  :phenotype packed-phenotype
;;                                  :type type-name
;;                                  :nearby (if nearby? "1" "0")}))]
;;         (assoc-showall result* nearby?))))

(defn- obj-interaction
  [nearby? data [interaction
                     {:keys [type-name effector affected direction]}]]
  (let [roles [effector affected]]
    (cond
      (not-any? interactor roles) data
      (some nil? roles) data
      :default (process-obj-interaction nearby?
                                        data
                                        interaction
                                        type-name
                                        effector
                                        affected
                                        direction))))

(defn- obj-interactions
  [obj data & {:keys [nearby?]}]
  (let [ints (gene-interaction-tuples obj nearby?)
        db (d/entity-db obj)
        mk-interaction (partial obj-interaction nearby?)
        mk-pair-wise (fn [[interaction holder1 holder2]]
                       (map vector
                            (repeat (d/entity db interaction))
                            (interaction-info (d/entity db interaction)
                                              (d/entity db holder1)
                                              (d/entity db holder2)
                                              nearby?)))]
    (if (and nearby? (> (count ints) 3000))
      (assoc data :showall "0")
      (reduce mk-interaction data (mapcat mk-pair-wise ints)))))


(defn- collect-phenotypes
  "Collect phenotypes from node edges."
  [edges]
  (->> edges
       (map :phenotype)
       (filter identity)
       (set)
       (map (fn [pt]
              [(:id pt) pt]))
       (into {})
       (not-empty)))

(defn- build-interactions [interactions interactions-nearby arrange-results]
  (let [edge-vals (comp vec fixup-citations vals :edges)
        data interactions
        edges (edge-vals data)
        results interactions-nearby
        edges-all (edge-vals results)]
    (-> results
        (assoc :phenotypes (collect-phenotypes edges-all))
        (arrange-results edges edges-all))))

(defn- arrange-interactions [results edges edges-all]
  (if (:showall results)
    (-> (assoc results :edges edges)
        (assoc :edges_all edges-all)
        (assoc :class "Gene")
        (assoc :showall "1"))
    {:edges edges}))

(defn- arrange-interaction-details [results edges edges-all]
  (-> results
      (assoc :edges edges-all)
      (update-in [:showall] #(str (if % 1 0)))))

(defn interactions
  "Produces a data structure suitable for rendering the table listing."
  [gene]
  {:description "genetic and predicted interactions"
   :data (let [data (obj-interactions gene {} :nearby? false)
               results (obj-interactions gene {} :nearby? true)]
           (build-interactions data results arrange-interactions))})

(defn interaction-details
  "Produces a data-structure suitable for rendering a cytoscape graph."
  [gene]
  {:description "addtional nearby interactions"
   :data (let [data (obj-interactions gene {} :nearby? false)
               results (obj-interactions gene {} :nearby? true)]
           (build-interactions data results arrange-interaction-details))})

(def widget
  {:name generic/name-field
   :interactions interactions})
