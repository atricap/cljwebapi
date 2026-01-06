(ns cljwebapi.auth.account-service.in-memory
  (:require
    [cljwebapi.auth.account-service :as account-service :refer [load-by-username]]
    [integrant.core :as ig]))

(defmethod load-by-username ::account-service/in-memory
  [this username]
  (->> (:provider this)
       (filter #(= username (:username %)))
       first))

(defn- create-provider
  []
  #{{:username "ushi@example.com"
     :password "{bcrypt}$2a$08$S44t9otZ/FSlegmOseYnwOoecLL8hUautRPMfKnJJTB7wJm6oxTji"
     :authorities #{"ROLE_EMPLOYEE"}
     :enabled? true
     :non-locked? true
     :non-expired? true
     :credentials-non-expired? true}
    {:username "toshi@example.com"
     :password "{bcrypt}$2a$08$/PAIhj05RW6XhLkGHZT8bORTAikv8HwENsoMRZqeJtChjvlORQOYO"
     :authorities #{"ROLE_EMPLOYEE" "ROLE_ADMIN"}
     :enabled? true
     :non-locked? true
     :non-expired? true
     :credentials-non-expired? true}})

(defmethod ig/init-key :auth.account-service/in-memory
  [_ {:keys [] :as opts}]
  (assoc opts :type ::account-service/in-memory
              :provider (create-provider)))

