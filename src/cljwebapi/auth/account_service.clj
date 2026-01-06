(ns cljwebapi.auth.account-service)

(defmulti load-by-username
  "Loads the user-details by given username"
  (fn [this username]
    (:type this)))

