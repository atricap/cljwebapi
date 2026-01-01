(ns cljwebapi.system
  (:require
    [cljwebapi.db]
    [cljwebapi.router]
    [cljwebapi.server]
    [integrant.core :as ig]))

(defn start
  [config]
  (println "- starting system...") ;TODO delete
  (ig/init config))

(defn stop
  [system]
  (println "- stopping system...") ;TODO delete
  (ig/halt! system))

