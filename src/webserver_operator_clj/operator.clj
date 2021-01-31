(ns webserver-operator-clj.operator
  (:require
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre]
   [webserver-operator-clj.controller :as controller])
  (:import
   [io.fabric8.kubernetes.client
    ConfigBuilder
    DefaultKubernetesClient]
   [io.javaoperatorsdk.operator
    Operator]))

(defn start []
  (let [config (-> (ConfigBuilder.)
                   (.withNamespace nil)
                   (.build))
        client (DefaultKubernetesClient. config)
        operator (Operator. client (controller/configuration-service))]
    (-> operator
        (.register (controller/new client)))
    operator))

(defrecord OperatorComponent [options]
  component/Lifecycle
  (start [this]
    (let [operator-name (:name options)]
      (timbre/infof "Starting operator: %s..." operator-name)
      (let [operator (start)]
        (timbre/infof "Starting operator %s completed." operator-name)
        (assoc this :operator operator))))
  (stop [this]
    (let [operator-name (:name options)]
      (timbre/infof "Stopping operator %s..." operator-name)
      (let [operator (:operator this)]
        (when operator
          (timbre/infof "stop")))
      (timbre/infof "Stopping operator %s completed." operator-name)
      (assoc this :operator nil))))

(defn start-operator [options]
  (map->OperatorComponent
   {:options options}))
