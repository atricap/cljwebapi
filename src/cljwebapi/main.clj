(ns cljwebapi.main
  (:require
    [cljwebapi.config :as config]
    [cljwebapi.system :as system]))

(defn -main
  [& args]
  (prn config/config) ;TODO delete
  (let [system (-> config/config system/start)]
    (->> (Thread. #(system/stop system))
         (.addShutdownHook (Runtime/getRuntime)))
    system))

