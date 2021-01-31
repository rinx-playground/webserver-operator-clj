(ns webserver-operator-clj.system
  (:require
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre]
   [webserver-operator-clj.server :as server]
   [webserver-operator-clj.operator :as operator]))

(defn system [{:keys [liveness operator readiness] :as conf}]
  (component/system-map
   :liveness (server/start-server liveness)
   :operator (component/using
              (operator/start-operator operator)
              {:liveness :liveness})
   :readiness (component/using
               (server/start-server readiness)
               {:operator :operator})))
