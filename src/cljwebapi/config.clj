(ns cljwebapi.config
  (:require
    [clojure.java.io :as io]
    [aero.core :as aero]
    [integrant.core :as ig]))

(defmethod aero/reader 'ig/ref
  [{:keys [profile] :as _opts} _tag value]
  (ig/ref value))

(defn get-config
  []
  (aero/read-config (io/resource "config.edn")))

(def config
  (get-config))

