(ns rest-api.main
  (:require
   [compojure.api.sweet :as sweet]
   [environ.core :as environ]
   [mount.core :as mount]
   [rest-api.classes.gene :as gene]
   [rest-api.classes.transcript :as transcript]))

(def ^:private all-routes
  "A collection of all routes to served by the application."
  [gene/routes
   transcript/routes])

(def ^:private swagger-validator-url
  "The URL used to validate the swagger JSON produced by the application."
  (if-let [validator-url (environ/env :swagger-validator-url)]
    validator-url
    "//online.swagger.io"))

(def ^:private api-output-formats
  "The formats API endpoints will produce data in."
  ["application/json"])

(defn init
  "Entry-point for ring server initialization."
  []
  (mount/start))

(def app
  "Entry-point for ring request handler."
  (sweet/api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :formats {:format api-output-formats}
     :coercion nil
     :consumes nil
     :produces api-output-formats
     :definitions {}
     :options
     {:ui
      {:validatorUrl swagger-validator-url}}
     :data
     {:info
      {:title "WormBase REST API"
       :description
       (str "Widget and field endpoints "
            "used by the official [WormBase]"
            "(http://www.wormbase.org) site.")
       :contact {:name "WormBase development team"
                 :email "developers@wormbase.org"}
       :version (System/getProperty "rest-api.version")}}}}
   (sweet/context "/" []
     :middleware [ring-gzip/wrap-gzip]
     (sweet/context "/rest" []
       (->> all-routes
            (flatten)
            (apply sweet/routes))))))

