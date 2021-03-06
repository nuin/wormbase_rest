(ns rest-api.classes.anatomy-term
  (:require
    [rest-api.classes.gene.widgets.external-links :as external-links]
    [rest-api.classes.anatomy-term.widgets.overview :as overview]
    [rest-api.classes.anatomy-term.widgets.associations :as associations]
    [rest-api.classes.anatomy-term.widgets.expression-markers :as expression-markers]
    [rest-api.classes.anatomy-term.widgets.ontology-browser :as ontology-browser]
    [rest-api.routing :as routing]))

(routing/defroutes
  {:entity-ns "anatomy-term"
   :widget
   {:overview overview/widget
    :ontology_browser ontology-browser/widget
    :associations associations/widget
    :expression_markers expression-markers/widget
    :external_links external-links/widget}})
