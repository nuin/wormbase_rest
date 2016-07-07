(ns datomic-rest-api.get-handler
  (:use ring.middleware.stacktrace
        ring.middleware.params
        ring.middleware.keyword-params
        ring.middleware.multipart-params
        clojure.walk
        pseudoace.utils)
  (:require [datomic.api :as d :refer (db history q touch entity)]
            [clojure.string :as str]
            [ring.adapter.jetty :refer (run-jetty)]
            [compojure.core :refer (defroutes GET POST ANY context wrap-routes)]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :refer [redirect file-response]]
            [cheshire.core :as json :refer [parse-string]]
            [environ.core :refer (env)]
            [datomic-rest-api.rest.gene :as gene]
            [datomic-rest-api.rest.interactions :refer (get-interactions get-interaction-details)]
            [datomic-rest-api.rest.references :refer (get-references)]
            [datomic-rest-api.rest.locatable-api :refer (feature-api)]))

(def uri (env :trace-db))
(def con (d/connect uri))

(defroutes get-routes
  (GET "/rest/widget/gene/:id/overview" {params :params}
       (gene/overview (db con) (:id params) (str "rest/widget/gene/" (:id params) "/overview")))
  (GET "/rest/widget/gene/:id/history" {params :params}
       (gene/history (db con) (:id params) (str "rest/widget/gene/" (:id params) "/history"))) ;; perfect match
  (GET "/rest/widget/gene/:id/phenotype" {params :params}
       (gene/phenotypes (db con) (:id params) (str "rest/widget/gene/" (:id params) "/phenotype"))) ;; broken because of variation/phenotype
  (GET "/rest/widget/gene/:id/interactions" {params :params}
       (get-interactions "gene" (db con) (:id params) (str "rest/widget/gene/" (:id params) "/interactions"))) ;; needed work on nodes all - not quite lining up
  (GET "/rest/field/gene/:id/interaction_details" {params :params}
       (get-interaction-details "gene" (db con) (:id params) (str "rest/field/gene/" (:id params) "/interaction_details"))) ;; wormbase is missing data section: why?
  (GET "/rest/widget/gene/:id/mapping_data" {params :params}
       (gene/mapping-data (db con) (:id params) (str "rest/widget/gene/" (:id params) "/mapping_data")))
  (GET "/rest/widget/gene/:id/human_diseases" {params :params}
       (gene/human-diseases (db con) (:id params) (str "rest/widget/gene/" (:id params) "/human_disease")))
  (GET "/rest/widget/gene/:id/references" {params :params}
       (get-references "gene" (db con) (:id params) (str "rest/widget/gene/" (:id params) "/references")))
  (GET "/rest/widget/gene/:id/reagents" {params :params}
       (gene/reagents (db con) (:id params) (str "rest/widget/gene/" (:id params) "/reagents"))) ;; looks correct; needs sort to confirm
  (GET "/rest/widget/gene/:id/gene_ontology" {params :params}
       (gene/gene-ontology (db con) (:id params) (str "rest/widget/gene/" (:id params) "/gene_ontology")))
  (GET "/rest/widget/gene/:id/expression" {params :params}
       (gene/expression (db con) (:id params) (str "rest/widget/gene/" (:id params) "/expression")))
  (GET "/rest/widget/gene/:id/homology" {params :params}
       (gene/homology (db con) (:id params) (str "rest/widget/gene/" (:id params) "/homology"))) ;; need to wait for homology data to be added to datomic database
  (GET "/rest/widget/gene/:id/sequences" {params :params}
       (gene/sequences (db con) (:id params) (str "rest/widget/gene/" (:id params) "/sequences")))
  (GET "/rest/widget/gene/:id/feature" {params :params}
       (gene/features (db con) (:id params) (str "rest/widget/gene/" (:id params) "/feature")))
  (GET "/rest/widget/gene/:id/genetics" {params :params}
       (gene/genetics (db con) (:id params) (str "rest/widget/gene/" (:id params) "/genetics")))
  (GET "/rest/widget/gene/:id/external_links" {params :params}
       (gene/external-links (db con) (:id params) (str "rest/widget/gene/" (:id params) "/overview"))))

(def trace-port (let [p (env :trace-port)]
                  (cond
                   (integer? p)  p
                   (string? p)   (parse-int p)
                   :default      8120)))

(defonce server
    (run-jetty get-routes {:port trace-port}))
