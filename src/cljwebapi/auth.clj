(ns cljwebapi.auth
  (:require
    [cljwebapi.auth.account-service :as account-service]
    [integrant.core :as ig]
    [buddy.sign.jwt :as jwt]
    [buddy.core.codecs :as codecs]
    [crypto.password.bcrypt :as password]))

(defn- sign
  [claims {:keys [issuer expiration secret]}]
  (let [now (java.time.Instant/now (java.time.Clock/systemUTC))
        expiration (java.time.Duration/ofSeconds expiration)
        claims (merge {:iat now
                       :exp (.plus now expiration)}
                      claims)
        opts {:alg :hs256
              :iss issuer
              :token-name "Bearer"}
        key-bytes (-> secret
                      codecs/str->bytes
                      codecs/b64->bytes)
        token (jwt/sign claims key-bytes opts)]
    token))

(defn pre-authentication-checks
  [account]
  (and (:enabled? account)
       (:non-locked? account)
       (:non-expired? account)))

(def stored-password-pattern
  (re-pattern #"^\{([^}]*)\}(.*)$"))

(defn- password-matches?
  [presented-password password]
  (let [[_ algorithm password] (re-find stored-password-pattern password)]
    (when (.equalsIgnoreCase "bcrypt" algorithm)
      (password/check presented-password password))))

(defn authentication-checks
  [account authentication-request]
  (password-matches? (:password authentication-request) (:password account)))

(defn post-authentication-checks
  [account]
  (:credentials-non-expired? account))

(defn authenticate
  [{:keys [account-service] :as auth}
   {:keys [username password] :as authentication-request}]
  (let [account (account-service/load-by-username account-service username)]
    (and (pre-authentication-checks account)
         (authentication-checks account authentication-request)
         (post-authentication-checks account))))

(defn login
  [auth {:keys [username] :as authentication-request}]
  (let [opts auth]
    (when (authenticate auth authentication-request)
      (sign {:sub username} opts))))

(defmethod ig/init-key :auth/jwt
  [_ {:keys [] :as opts}]
  opts)

