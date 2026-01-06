(ns cljwebapi.auth.account-service.db
  (:require
    [cljwebapi.auth.account-service :as account-service :refer [load-by-username]]
    [integrant.core :as ig]))

(defmethod load-by-username ::account-service/db
  [this username]
  nil)

(defn- create-provider ;FIXME
  []
  nil)

(defmethod ig/init-key :auth.account-service/db
  [_ {:keys [] :as opts}]
  (assoc opts :type ::account-service/db
              :provider (create-provider)))

