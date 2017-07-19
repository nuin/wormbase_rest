(ns rest-api.classes.anatomy-term
  (:require
    [rest-api.classes.gene.widgets.external-links :as external-links]
    ;[rest-api.classes.anatomy-term.widgets.overview :as overview]
    [rest-api.classes.anatomy-term.widgets.references :as references]
    [rest-api.classes.anatomy-term.widgets.ontology-browser :as ontology-browser]
    [rest-api.routing :as routing]))

(routing/defroutes
  {:entity-ns "anatomy-function"
   :uri-name "anatomy_term"
   :widget
   {;:overview overview/widget
    :ontology_browser ontology-browser/widget
    :external_links external-links/widget
    :references references/widget}})
