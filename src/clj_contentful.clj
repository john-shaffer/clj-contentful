(ns clj-contentful
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [cemerick.url :as url]))

(def ^:const cda-server "https://cdn.contentful.com")

(def ^{:dynamic true} *config* nil)

(defmacro defn-wrap
  "Like defn, but applies wrap-fn."
  [name-sym wrap-fn & body]
  `(do
     (defn ~name-sym ~@body)
     (alter-var-root #'~name-sym ~wrap-fn)))

(defmacro with-config [config & body]
  `(binding [*config* ~config]
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

(defop request [config f path]
  (let [{:keys [access-token server space-id]} config]
    (-> (f (str (url/url server path))
           {:headers {:Authorization (str "Bearer " access-token)}}) ; OAuth 2.0
         :body
         (json/parse-string true))))

(defop cda-request
  "Makes a GET request to the Content Delivery API. Returns the response body
  parsed as a map."
  [config subpath]
  (let [config (if (:server config)
                 config
                 (assoc config :server cda-server))]
    (with-config config
      (request client/get (str "spaces/" (:space-id config) "/" subpath)))))

(defop get-space
  "Gets the space referred to by config."
  [config]
  (cda-request ""))

(defop space-entries [config]
  (cda-request "entries"))
