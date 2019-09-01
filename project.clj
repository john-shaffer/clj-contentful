(defproject clj-contentful "0.1.0-SNAPSHOT"
  :description "A Clojure library for the Contentful APIs."
  :url "https://github.com/john-shaffer/clj-contentful"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :deploy-repositories [["releases" :clojars
                         "snapshots" :clojars]]
  :repl-options {:init-ns clj-contentful})
