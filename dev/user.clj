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
    [cljwebapi.auth :as auth]
    [cljwebapi.auth.account-service :as acc]
    [buddy.sign.jwt :as jwt]
    [buddy.core.codecs :as codecs]
    [integrant.core :as ig]
    [integrant.repl :refer [clear set-prep! prep init go halt reset reset-all]]
    [integrant.repl.state :as state]))

(comment
  (set-prep! (fn [] (-> (config/get-config :dev)
                        #_(select-keys [:web/server :web/router :db/mysql :auth/jwt]))))
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

  (let [now (java.time.Instant/now (java.time.Clock/systemUTC))
        expiration (java.time.Duration/ofMinutes 15)
        claims {:sub "eric@example.com"
                :iat now
                :exp (.plus now expiration)}
        opts {:alg :hs256
              :iss "de.atricap.cljwebapi"
              :token-name "Bearer"}
        key-bytes (-> "OJqjRU3auDYLBB0UCwmwEAXGAiqkH5DFRlSjUhG8Oxw="
                      codecs/str->bytes
                      codecs/b64->bytes)
        token (jwt/sign claims key-bytes opts)
        ;opts (assoc opts :iss "foreigner")
        result (jwt/unsign token key-bytes opts)]
    token)

  (let [auth (:auth/jwt state/system)
        {:keys [account-service]} auth
        areq {:username "ushi@example.com" :password "ushi1"}
        {:keys [username password]} areq
        account (acc/load-by-username account-service username)]
    #_(auth/password-matches? password (:password account))
    #_account
    (auth/login auth areq))
  )

