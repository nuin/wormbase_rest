(ns rest-api.classes.molecule
  (:require
    [rest-api.classes.gene.widgets.external-links :as external-links]
    [rest-api.classes.molecule.widgets.overview :as overview]
    [rest-api.classes.molecule.widgets.structure :as structure]
    [rest-api.classes.molecule.widgets.strains :as strains]
    [rest-api.classes.molecule.widgets.affected :as affected]
    [rest-api.classes.molecule.widgets.references :as references]
    [rest-api.routing :as routing]))

(routing/defroutes
  {:entity-ns "molecule"
   :widget
   {:overview overview/widget
    :structure structure/widget
    :strains strains/widget
    :affected affected/widget
    :external_links external-links/widget
    :references references/widget}})
