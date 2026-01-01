(ns cljwebapi.server
  (:require
    [integrant.core :as ig]
    [ring.adapter.jetty :as j]))

(defn- get-jetty-config
  [opts]
  (select-keys opts [:port :join?]))

(defn get-app
  [{{:keys [router ring-handler]} :router :as _opts}]
  ring-handler)

(defmethod ig/init-key :web/server
  [_ {:keys [] :as opts}]
  (assoc opts
         :jetty (j/run-jetty (get-app opts)
                             (get-jetty-config opts))))

(defmethod ig/halt-key! :web/server
  [_ {:keys [jetty] :as _opts}]
  (.stop jetty))

