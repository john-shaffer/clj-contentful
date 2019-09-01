(ns clj-contentful
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [cemerick.url :as url]))

(def ^:const cda-server "https://cdn.contentful.com")

(defn request [config f path]
  (let [{:keys [access-token server space-id]} config]
    (->> {:headers {:Authorization (str "Bearer " access-token)}} ; OAuth 2.0
         (f (str (url/url server path)))
         :body
         json/parse-string)))

(defn cda-request
  "Make a GET request to the Content Delivery API. Returns the response body
  parsed as a map."
  [config subpath]
  (let [config (if (:server config)
                 config
                 (assoc config :server cda-server))]
    (request config client/get (str "spaces/" (:space-id config) "/" subpath))))

(defn space-entries [config]
  (cda-request config "entries"))
