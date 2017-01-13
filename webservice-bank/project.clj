(defproject webservice-bank "0.1.0-SNAPSHOT"
 :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.3"]
                 [ring "1.2.0"]
                 [compojure "1.1.6"]
                 [clj-time "0.13.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler webservice-bank.handler/full-handler
         :port 3001
         :init webservice-bank.handler/on-init
         :destroy webservice-bank.handler/on-destroy})
