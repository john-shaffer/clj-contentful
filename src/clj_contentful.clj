(ns clj-contentful
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [cemerick.url :as url]))

(defn request [config f path]
  (let [{:keys [access-token server space-id]} config]
    (->> {:headers {:Authorization (str "Bearer " access-token)}}
         (f (str (url/url server path)))
         :body
         json/parse-string)))

(defn get [config path]
  (request config client/get path))

(defn space-entries [config]
  (get config (str "spaces/" (:space-id config) "/entries")))
