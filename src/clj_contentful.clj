(ns clj-contentful
  (:refer-clojure :exclude (symbol? type))
  (:use clj-contentful.util)
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [cemerick.url :as url]))

(def ^:const cda-server "https://cdn.contentful.com")

(def ^:dynamic *config* nil)

(defn metafy [m]
  (with-meta (:fields m)
    (dissoc m :fields)))

(defn type
  "Returns the type of a map in the JSON response that has a :type key in
  (:sys m) or in m."
  [m]
  (or (:type (:sys m)) (:type m)))

(defn array? [m]
  (= "Array" (type m)))

(defn asset? [m]
  (= "Asset" (type m)))

(defn entry? [m]
  (= "Entry" (type m)))

(defn link? [m]
  (= "Link" (type m)))

(defn locale? [m]
  (= "Locale" (type m)))

(defn space? [m]
  (= "Space" (type m)))

(defn symbol? [m]
  (= "Symbol" (type m)))

(defn parse-config
  "If config is a vector of [space-id access-token & [environment]], converts it
  to a map. Otherwise, returns config."
  [config]
  (if (vector? config)
    (let [[space-id access-token environment] config]
      {:space-id space-id, :access-token access-token,
       :environment (or environment "master")})
    config))

(defmacro with-config
  "Binds the dynamic var clj-contentful/*config* to config, allowing code inside
  the body to forgo passing config to any function defined with defop."
  [config & body]
  `(binding [*config* (parse-config ~config)]
     ~@body))

(defn wrap-op
  "Wraps f in a function that provides the first argument whever *config* is
  thread-bound."
  [f]
  (fn [& [maybe-config & more :as args]]
    (if (and (thread-bound? #'*config*)
             (not (nil? *config*)))
      (apply f *config* args)
      (with-config maybe-config
        (apply f maybe-config more)))))

(defmacro defop
  "Same as defn, but wraps the defined function in another that provides the
  first argument whenever *config* is thread-bound."
  [name-sym & body]
  `(do
     (defn-wrap ~name-sym wrap-op ~@body)
     (alter-meta! (var ~name-sym) update-in [:doc] str
       "\n\n  When used within the dynamic scope of `with-config`, the initial"
       "\n  `config` argument is automatically provided.")))

(defn handle-array
  "Returns just the :items from an Array response."
  [m]
  (:items m))

(def type-handlers
  {"Array" handle-array})

(defop request
  "Makes a request to Contentful's servers. f determines the method used. f
  should be one of clj-http.client/get, /put, etc., or a compatible function.
  https://www.contentful.com/developers/docs/references/authentication/"
  [config f & [path query-params]]
  (let [{:keys [access-token server space-id]} config
        body (-> (f (str (url/url server path))
                    ; OAuth 2.0
                    {:headers {:Authorization (str "Bearer " access-token)}
                     :query-params query-params})
                 :body
                 (json/parse-string true))
        handler (-> body type type-handlers)]
    (if handler
      (handler body)
      body)))

(defop cda-request
  "Makes a GET request to the Content Delivery API. Returns the response body
  parsed as a map."
  [config & [subpath query-params]]
  (let [config (if (:server config)
                 config
                 (assoc config :server cda-server))]
    (with-config config
      (request client/get (str "spaces/" (:space-id config) "/" subpath)
               query-params))))

(defop cda-env-request
  "Makes a GET request to the environment set in config. Returns the response
  body parsed as a map."
  [config & [subpath query-params]]
  (cda-request
   (str "environments/" (:environment config "master") "/" subpath)
   query-params))

(defop space
  "Gets the space referred to by config.
  https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/spaces/space"
  [config]
  (cda-request))

(defop content-types
  "Gets the content model of a space.
  https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/content-types/content-model"
  [config]
  (cda-env-request "content_types"))

(defop content-type
  "Gets a single content type.
  https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/content-types/content-type"
  [config content-type-id]
  (cda-env-request (str "content_types/" content-type-id)))

(defop entries
  "Gets all entries of a space.
  https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/entries/entries-collection"
  [config & [query-params]]
  (map metafy
       (cda-env-request "entries" query-params)))

(defop entry
  "Gets a single entry.
  https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/entries/entry"
  [config entry-id]
  (metafy
   (cda-env-request (str "entries/" entry-id))))

(defop assets
  "Gets all assets of a space.
  https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/assets/assets-collection"
  [config]
  (map metafy
       (cda-env-request "assets")))

(defop asset
  "Gets a single asset.
  https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/assets/asset"
  [config asset-id]
  (metafy
   (cda-env-request (str "assets/" asset-id))))

(defop locales
  "Gets all locales of a space.
  https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/locales/locale-collection"
  [config]
  (cda-env-request "locales"))
