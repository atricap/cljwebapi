(ns cljwebapi.domain.user
  (:require
    [next.jdbc :as jdbc]))

(defn get-all
  [conn]
  (->> (jdbc/execute! conn [(str "SELECT u.id, u.first_name, u.last_name, u.email, a.authority"
                                 " FROM users u"
                                 " JOIN user_authorities a ON a.user_id = u.id")])
       (group-by #(select-keys % [:users/id
                                  :users/first_name
                                  :users/last_name
                                  :users/email]))
       (map (fn [[k vs]]
              (-> k
                  (assoc :users/full_name   (str (:users/first_name k) " " (:users/last_name k))
                         :users/authorities (map :user_authorities/authority vs))
                  (dissoc :users/first_name
                          :users/last_name))))))

;; FIXME: use auth to retrieve user
(defn get-authenticated-user
  [conn]
  (->> (jdbc/execute! conn [(str "SELECT u.id, u.first_name, u.last_name, u.email, a.authority"
                                 " FROM users u"
                                 " JOIN user_authorities a ON a.user_id = u.id"
                                 " WHERE u.id = 1")])
       (group-by #(select-keys % [:users/id
                                  :users/first_name
                                  :users/last_name
                                  :users/email]))
       (map (fn [[k vs]]
              (-> k
                  (assoc :users/full_name   (str (:users/first_name k) " " (:users/last_name k))
                         :users/authorities (map :user_authorities/authority vs))
                  (dissoc :users/first_name
                          :users/last_name))))
       first))


