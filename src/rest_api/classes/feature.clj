(ns rest-api.classes.feature
  (:require
    [rest-api.classes.feature.widgets.overview :as overview]
    [rest-api.classes.feature.widgets.location :as location]
    [rest-api.routing :as routing]))

(routing/defroutes
  {:entity-ns "feature"
   :widget
   {:overview overview/widget
    :location location/widget}})
