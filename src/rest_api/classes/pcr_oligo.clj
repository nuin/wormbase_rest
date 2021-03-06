(ns rest-api.classes.pcr-oligo
  (:require
    [rest-api.classes.pcr-oligo.widgets.overview :as overview]
    [rest-api.classes.pcr-oligo.widgets.location :as location]
    [rest-api.classes.pcr-oligo.widgets.sequence :as sequences]
    [rest-api.routing :as routing]))

(routing/defroutes
  {:entity-ns "pcr-oligo"
   :widget
   {:overview overview/widget
    ;:sequence sequences/widget
    :location location/widget}})
