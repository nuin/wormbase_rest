(ns rest-api.classes.construct
  (:require
    ;[rest-api.classes.construct.widgets.overview :as overview]
    [rest-api.routing :as routing]))

(routing/defroutes
  {:entity-ns "construct"
   :widget
   {;:overview overview/widget
    }})