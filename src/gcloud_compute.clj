(ns gcloud-compute
  "Google Cloud Compute API client."
  (:import
    (com.google.cloud.compute.v1
      InstancesClient)))


(defn- Instance
  [instance]
  {:name (.getName instance)})


(defn- create-instances-client
  []
  (InstancesClient/create))


(defn list-instances
  "List all compute instances."
  [project regions]
  (let [client (create-instances-client)]
    (try
      (->> regions
           (map (fn [region]
                  (-> client
                      (.list project region)
                      .iterateAll)))
           (reduce concat [])
           (map Instance))
      (finally (.close client)))))
