(ns cljwebapi.db
  (:require
    [integrant.core :as ig]
    [next.jdbc.connection :as conn])
  (:import
    (com.zaxxer.hikari HikariDataSource)))

(defmethod ig/init-key :db/mysql
  [_ {:keys [] :as opts}]
  (let [                  db-spec opts
        ^HikariDataSource ds      (conn/->pool HikariDataSource db-spec)]
    (assoc opts :datasource ds)))

(defmethod ig/halt-key! :db/mysql
  [_ {:keys [datasource] :as _opts}]
  (.close datasource)
  nil)

