(ns user
  (:require
    [cljwebapi.config :as config]
    [cljwebapi.db :as db]
    [cljwebapi.router :as router]
    [reitit.core :as r]
    [muuntaja.core :as m]
    [muuntaja.middleware :as mu.middleware]
    [malli.provider :as ma-provider]
    [cljwebapi.server :as server]
    [cljwebapi.system :as system]
    [cljwebapi.domain.user :as user]
    [integrant.core :as ig]
    [integrant.repl :refer [clear set-prep! prep init go halt reset reset-all]]
    [integrant.repl.state :as state]))

(comment
  (set-prep! (fn [] (-> (config/get-config :dev)
                        (select-keys [:web/server :web/router :db/mysql]))))
  state/config (keys state/config)
  (-> state/system :web/router :router r/compiled-routes) (keys state/system) (get-in state/system [:web/server :db])

  (let [r (-> state/system :web/router :router)
        h (-> state/system :web/router :ring-handler)]
    (-> {:uri "/api/openapi.json" :request-method :get}
          h :body slurp println)
    #_(r/match-by-path r "/api/users/info")
    #_(r/options r)
    )

  (let [db (-> state/system :db/mysql)]
    (-> (user/get-all db) first #_:users/email)
    )
  (with-open [^HikariDataSource ds (conn/->pool HikariDataSource db-spec)
              conn                 (jdbc/get-connection ds)]
    )
  )

