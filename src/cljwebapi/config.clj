(ns cljwebapi.config
  (:require
    [clojure.java.io :as io]
    [aero.core :as aero]
    [integrant.core :as ig]))

(defmethod aero/reader 'ig/ref
  [{:keys [profile] :as _opts} _tag value]
  (ig/ref value))

(defn get-config
  ([]
   (get-config :prod))
  ([profile]
   (aero/read-config (io/resource "config.edn") {:profile profile})))

(def config ;; :prod config cache
  (get-config))

