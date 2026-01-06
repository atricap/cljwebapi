(ns cljwebapi.router
  (:require
    [cljwebapi.config :as config]
    [cljwebapi.auth :as auth]
    [cljwebapi.domain.schema :as schema]
    [cljwebapi.domain.user :as user]
    [cljwebapi.domain.todo :as todo]
    [clojure.string :as str]
    [integrant.core :as ig]
    [ring.util.response :as res]
    [muuntaja.core :as mu]
    [muuntaja.middleware :as mu-mw]
    [reitit.ring.middleware.muuntaja :as mw-muuntaja]
    [reitit.ring.middleware.exception :as mw-exception]
    [reitit.middleware :as mw]
    [reitit.ring :as ring]
    [reitit.coercion.malli]
    [reitit.ring.coercion :as coercion]
    [reitit.openapi :as openapi]
    [reitit.swagger-ui :as swagger-ui]))

(defn hello-handler
  [_request]
  (-> (res/response "Hello World")
      (res/content-type "text/html")))

(defn bye-handler
  [_request]
  (-> (res/response "Bye World")
      (res/content-type "text/html")))

;; USERS

(defn get-users-handler
  [{:keys [db] :as _request}]
  (res/response (user/get-all db)))

(defn get-user-info-handler
  [{:keys [db] :as _request}]
  (res/response (user/get-authenticated-user db)))

;; AUTH

(defn login-handler
  [{{{:keys [email password]} :request} :parameters, :keys [auth], :as _request}]
  (let [token (auth/login auth {:username email :password password})]
    (if token
      (res/response {:token token})
      (res/bad-request {:status 400
                        :message "Bad credentials"
                        :timestamp (.toEpochMilli (java.time.Instant/now (java.time.Clock/systemUTC)))}))))

;;

(defn- status+body->response
  [status body]
  {:status  status
   :headers {}
   :body    body})

(defn not-found-handler
  [_request]
  (-> (status+body->response 404 "Not found!")
      (res/content-type "text/html")))

(defn method-not-allowed-handler
  [_request]
  (-> (status+body->response 405 "Method not allowed!")
      (res/content-type "text/html")))

(defn not-acceptable-handler
  [_request]
  (-> (status+body->response 406 "Not acceptable!")
      (res/content-type "text/html")))

(def routes
  [["/hello"           {:get hello-handler}]
   ["/bye"             {:get bye-handler}]
   ["/api"             {:middleware [::db-middleware
                                     ::auth-middleware]}
    ["/users"          {:tags #{"User REST API Endpoints"}}
     [""               {:get {:handler get-users-handler
                              :summary "All users"
                              :description "Get all users"
                              :responses {200 {:description "OK"
                                               :content {"application/json"
                                                         {:schema (schema/list-of schema/User)
                                                          :examples {:Two
                                                                     {:description "Two example users"
                                                                      :value '(#:users{:id 1
                                                                                       :email "eric@example.com"
                                                                                       :full_name "Eric Smith"
                                                                                       :authorities ("ROLE_EMPLOYEE" "ROLE_ADMIN")}
                                                                               #:users{:id 2
                                                                                       :email "chad@example.com"
                                                                                       :full_name "Chad Miller"
                                                                                       :authorities ("ROLE_EMPLOYEE")})}}}
                                                         "application/edn"
                                                         {:schema (schema/list-of schema/User)
                                                          :examples {:Two
                                                                     {:description "Two example users"
                                                                      :value (pr-str '(#:users{:id 1
                                                                                               :email "eric@example.com"
                                                                                               :full_name "Eric Smith"
                                                                                               :authorities ("ROLE_EMPLOYEE" "ROLE_ADMIN")}
                                                                                       #:users{:id 2
                                                                                               :email "chad@example.com"
                                                                                               :full_name "Chad Miller"
                                                                                               :authorities ("ROLE_EMPLOYEE")}))}}}}}}}}]
     ["/info"          {:get {:handler get-user-info-handler
                              :summary "User information"
                              :description "Get current user info"
                              :responses {200 {:description "OK"
                                               :content {"application/json"
                                                         {:schema schema/User
                                                          :examples {:Eric
                                                                     {:description "Example user Eric"
                                                                      :value '#:users{:id 1
                                                                                      :email "eric@example.com"
                                                                                      :full_name "Eric Smith"
                                                                                      :authorities ("ROLE_EMPLOYEE" "ROLE_ADMIN")}}}}
                                                         "application/edn"
                                                         {:schema schema/User
                                                          :examples {:Eric
                                                                     {:description "Example user Eric"
                                                                      :value (pr-str '#:users{:id 1
                                                                                              :email "eric@example.com"
                                                                                              :full_name "Eric Smith"
                                                                                              :authorities ("ROLE_EMPLOYEE" "ROLE_ADMIN")})}}}}}}}}]]
    ["/auth"           {:tags #{"Authentication REST API Endpoints"}}
     ["/login"         {:post {:handler login-handler
                               :summary "Login a user"
                               :description "Submit email & password to authenticate user"
                               :request {:required true
                                         :content {:default
                                                   {:schema schema/LoginRequest
                                                    :examples {:Eric
                                                               {:description "Example login request with user Eric"
                                                                :value {:email "eric@example.com"
                                                                        :password "test1"}}}}}}
                               :responses {200 {:description "OK"
                                                :content {"application/json"
                                                          {:schema schema/LoginResponse
                                                           :examples {:Token
                                                                      {:description "Example token"
                                                                       :value `{:token ~(str "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlcmljQGV"
                                                                                             "4YW1wbGUuY29tIiwiaWF0IjoxNzY3NDMxMjQ1LCJ"
                                                                                             "leHAiOjE3Njc0MzIxNDUsImlzcyI6ImRlLmF0cml"
                                                                                             "jYXAuY2xqd2ViYXBpIn0.5RMZuL833tjAirOdkgn"
                                                                                             "iiiWk50FueKU7RD-fCBAHX5E")}}}}
                                                          "application/edn"
                                                          {:schema schema/LoginResponse
                                                           :examples {:Token
                                                                      {:description "Example token"
                                                                       :value (pr-str `{:token ~(str "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlcmljQGV"
                                                                                                     "4YW1wbGUuY29tIiwiaWF0IjoxNzY3NDMxMjQ1LCJ"
                                                                                                     "leHAiOjE3Njc0MzIxNDUsImlzcyI6ImRlLmF0cml"
                                                                                                     "jYXAuY2xqd2ViYXBpIn0.5RMZuL833tjAirOdkgn"
                                                                                                     "iiiWk50FueKU7RD-fCBAHX5E")})}}}}}}}}]]
    ["/openapi.json"   {:get {:handler (openapi/create-openapi-handler)
                              :no-doc true
                              :openapi {:info {:title "API Documentation" :version "v1"}
                                        :tags #{{:name "User REST API Endpoints"
                                                 :description "User REST API Endpoints"}
                                                {:name "Authentication REST API Endpoints"
                                                 :description "Operations related to register & login"}}}}}]]])

(def initial-route-data
  {::db nil ;; Placeholder, will be filled in in create-router
   ::auth nil ;; Placeholder, will be filled in in create-router
   :coercion reitit.coercion.malli/coercion
   :muuntaja mu/instance
   :middleware [openapi/openapi-feature
                ;; query-params & form-params
                ;parameters/parameters-middleware
                ;; content-negotiation
                mw-muuntaja/format-negotiate-middleware
                ;; encoding response body
                mw-muuntaja/format-response-middleware
                ;; exception handling
                mw-exception/exception-middleware
                ;; decoding request body
                mw-muuntaja/format-request-middleware
                ;; pretty coercion exceptions
                coercion/coerce-exceptions-middleware
                ;; coercing response bodys
                coercion/coerce-response-middleware
                ;; coercing request parameters
                coercion/coerce-request-middleware
                ;; multipart
                ;multipart/multipart-middleware
                ;::wrap-db
                ]})

(def db-middleware
  "Adds the db object to the request. Expects `::db` in the route data."
  {:name ::db
   :compile (fn [{::keys [db]} _] ; Extracts :db from the route data
              (fn [handler]
                (fn [request]
                  (handler (assoc request :db db)))))})

(def auth-middleware
  "Adds the auth object to the request. Expects `::auth` in the route data."
  {:name ::auth
   :compile (fn [{::keys [auth]} _] ; Extracts :auth from the route data
              (fn [handler]
                (fn [request]
                  (handler (assoc request :auth auth)))))})

(defn create-router
  [{:keys [db auth] :as _opts}]
  (ring/router routes {:data (merge initial-route-data {::db db, ::auth auth})
                       ::mw/registry {::db-middleware db-middleware
                                      ::auth-middleware auth-middleware}}))

(defn create-default-handler
  [_opts]
  (ring/routes ;; Combine following two handlers
    (swagger-ui/create-swagger-ui-handler ;; Has to be defined outside of routes
      {:path "/swagger-ui"
       :url "" ; Will be overridden by :urls, but wipe the default "/swagger.json"
       :config {:validatorUrl nil
                :urls [{:name "openapi", :url "/api/openapi.json"}]
                :urls.primaryName "openapi"
                :operationsSorter "alpha"}})
    (ring/create-default-handler
      {:not-found          not-found-handler
       :method-not-allowed method-not-allowed-handler
       :not-acceptable     not-acceptable-handler})))

(defn create-ring-handler
  [router default-handler]
  (-> (ring/ring-handler router default-handler)
      ;(mu-mw/wrap-format)
      ))

(defmethod ig/init-key :web/router
  [_ opts]
  (let [router          (create-router opts)
        default-handler (create-default-handler opts)
        ring-handler    (create-ring-handler router default-handler)]
    (assoc opts
           :router       router
           :ring-handler ring-handler)))

