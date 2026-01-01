(ns cljwebapi.domain.schema)

(defn list-of
  [elem]
  [:sequential elem])

(def User
  [:map
   [:users/id :int]
   [:users/email :string]
   [:users/full_name :string]
   [:users/authorities [:sequential :string]]])

