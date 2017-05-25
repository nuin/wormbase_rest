(ns rest-api.classes.molecule.widgets.overview
  (:require
   [clojure.string :as str]
   [datomic.api :as d]
   [pseudoace.utils :as pace-utils]
   [rest-api.classes.generic :as generic]
   [rest-api.formatters.date :as date]
   [rest-api.formatters.object :as obj :refer [pack-obj]]))

(defn detection-status [m]
  {:data (when-let [mshs (:molecule/status m)]
           (let [msh (first mshs)]
             {:text (:molecule.status/value msh)
              :evidence (obj/get-evidence msh)}))
   :description "Signifies if the molecule is predicted to be present in the animal or was shown to be present in the organism through a direct detection method"})

(defn extraction-method [m]
  {:data nil
   :description "Method used to extract the molecule during detection"})

(defn chembi-id [m]
  {:data (when-let [ds (:molecule/database m)]
           (first
             (not-empty
               (remove
                 nil?
                 (for [d ds
                       :let [database-name (:database/id
                                             (:molecule.database/database d))]]
                   (if (= database-name "ChEBI")
                     (:molecule.database/text d)))))))
   :description "ChEBI id of the molecule"})

(defn detection-method [m]
  {:data nil
   :description "Experimental tool used to detect molecule"})

(defn monoisotopic-mass [m]
  {:data nil
   :description "Monoisotopic mass calculated from the chemical formula of the molecule"})

(defn formula [m]
  {:data nil
   :description "Molecular formula from ChEBI"})

(defn synonyms [m]
  {:data nil
   :description "Other common names for the molecule"})

(defn iupac [m]
  {:data (first (:molecule/iupac m))
   :description "IUPAC name"})

(defn inchi-key [m]
  {:data (first (:molecule/inchikey m))
   :description "InChi structure key"})

(defn nonspecies-source [m]
  {:data nil
   :description "Source of molecule when not generated by the organism being studied"})

(defn molecule-use [m]
  {:data nil
   :description "Reported uses/affects of the molecule with regards to nematode species biology"})

(defn biological-role [m]
  {:data nil
   :desciption "Controlled vocabulary for specific role of molecule in nematode biology, with particular regards to biological pathways"})

(defn smiles [m]
  {:data (first (:molecule/smiles m))
   :description "SMILES structure"})

(defn inchi [m]
  {:data (first (:molecule/inchi m))
   :description "InChi structure"})

(defn biofunction-role [m]
  {:data nil
   :keys (keys m)
   :description "Controlled vocabulary for specific role of molecule in nematode biology, with particular regards to biological pathways"})

(def widget
  {:name generic/name-field
   :detection_status detection-status
   :extraction_method extraction-method
   :chembi_id chembi-id
   :detection_method detection-method
   :monoisotopic_mass monoisotopic-mass
   :formula formula
   :synonyms synonyms
   :remarks generic/remarks
   :iupac iupac
   :inchi_key inchi-key
   :nonspecies-source nonspecies-source
   :molecule_use molecule-use
   :biological_role biological-role
   :smiles smiles
   :inchi inchi
   :biofunction_role biofunction-role})
