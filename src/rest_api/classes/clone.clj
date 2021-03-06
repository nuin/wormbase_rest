(ns rest-api.classes.clone
  (:require
    [rest-api.classes.gene.widgets.external-links :as external-links]
    [rest-api.classes.clone.widgets.overview :as overview]
    [rest-api.classes.clone.widgets.location :as location]
    [rest-api.classes.clone.widgets.sequences :as sequences]
    [rest-api.classes.clone.widgets.references :as references]
    [rest-api.routing :as routing]))

(routing/defroutes
  {:entity-ns "clone"
   :widget
   {:overview overview/widget
    :location location/widget
    :sequences sequences/widget
    :external_links external-links/widget
    :references references/widget}})
