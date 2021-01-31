(ns webserver-operator-clj.controller
  (:require
   [taoensso.timbre :as timbre])
  (:import
   [io.fabric8.kubernetes.client
    CustomResource]
   [io.javaoperatorsdk.operator
    ControllerUtils]
   [io.javaoperatorsdk.operator.api
    DeleteControl
    ResourceController
    UpdateControl]
   [io.javaoperatorsdk.operator.api.config
    ControllerConfiguration]
   [io.javaoperatorsdk.operator.config.runtime
    DefaultConfigurationService]
   [io.javaoperatorsdk.operator.sample
    WebServer]))

(defn new [client]
  (reify
    ResourceController
    (createOrUpdateResource
      [this resource context]
      (let [ns (-> resource
                   (.getMetadata)
                   (.getNamespace))
            name (-> resource
                     (.getMetadata)
                     (.getName))
            html (-> resource
                     (.getSpec)
                     (.getHtml))]
        (timbre/infof "Execution createOrUpdateResource for %s" name)
        (UpdateControl/updateCustomResource resource)))
    (deleteResource
      [this resource context]
      (let [ns (-> resource
                   (.getMetadata)
                   (.getNamespace))
            name (-> resource
                     (.getMetadata)
                     (.getName))]
        (timbre/infof "Execution deleteResource for %s" name)
        (DeleteControl/DEFAULT_DELETE)))))

(defn configuration-service []
  (proxy [DefaultConfigurationService] []
    (getConfigurationFor [controller]
      (reify
        ControllerConfiguration
        (getName [this]
          (ControllerUtils/getNameFor controller))
        (getCRDName [this]
          (CustomResource/getCRDName WebServer))
        (getFinalizer [this]
          (ControllerUtils/getDefaultFinalizerName
            (CustomResource/getCRDName WebServer)))
        (isGenerationAware [this]
          true)
        (getCustomResourceClass [this]
          WebServer)
        (getAssociatedControllerClassName [this]
          (-> controller
              (.getClass)
              (.getCanonicalName)))))))
