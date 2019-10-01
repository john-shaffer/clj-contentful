(defproject clj-contentful "1.2.0-SNAPSHOT"
  :description "A Clojure library for the Contentful APIs."
  :url "https://github.com/john-shaffer/clj-contentful"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cheshire "5.9.0"]
                 [clj-http "3.10.0"]
                 [com.cemerick/url "0.1.1"]
                 [medley "1.2.0"]]
  :deploy-repositories [["releases" :clojars
                         "snapshots" :clojars]]
  :repl-options {:init-ns clj-contentful})
