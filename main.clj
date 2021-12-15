(ns user
  (:require
    [gcloud-compute]
    [mackerel]))


(def config
  {:gcloud-project-id (System/getenv "GCLOUD_PROJECT_ID")
   :mackerel-apikey (System/getenv "MACKEREL_APIKEY")
   :mackerel-role (System/getenv "MACKEREL_ROLE")
   :mackerel-service (System/getenv "MACKEREL_SERVICE")})


(let [{:keys [gcloud-project-id
              mackerel-apikey
              mackerel-role
              mackerel-service]} config
      compute-instances (gcloud-compute/list-instances gcloud-project-id
                                                       ["asia-northeast1-a"
                                                        "asia-northeast1-b"
                                                        "asia-northeast1-c"])
      compute-instance-names (map :name compute-instances)
      hosts (mackerel/list-hosts-in-role mackerel-apikey
                                         mackerel-service
                                         mackerel-role)
      missing-hosts (filter (fn [{:keys [name]}]
                              (->> compute-instance-names
                                   (some #{name})
                                   not))
                            hosts)]
  (doseq [host missing-hosts]
    (println "Retiring an host" host)
    (mackerel/retire-host mackerel-apikey (:id host))))
