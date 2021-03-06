(ns rest-api.classes.antibody
  (:require
    [rest-api.classes.antibody.widgets.overview :as overview]
    [rest-api.classes.antibody.widgets.expression :as expression]
    [rest-api.classes.antibody.widgets.references :as references]
    [rest-api.routing :as routing]))

(routing/defroutes
  {:entity-ns "antibody"
   :widget
   {:overview overview/widget
    :expression expression/widget
    :references references/widget}})
