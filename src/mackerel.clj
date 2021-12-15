(ns mackerel
  "Mackerel API client."
  (:require
    [clojure.data.json :as json]
    [org.httpkit.client :as http]))


(defn- Host
  [{:keys [id name]}]
  {:id id
   :name name})


(def api-endpoint "https://api.mackerelio.com")


(defn list-hosts-in-role
  "List hosts in the role."
  [apikey service-name role-name]
  (let [{:keys [status body error]} @(http/get (str api-endpoint "/api/v0/hosts")
                                               {:headers {"X-Api-Key" apikey}
                                                :query-params {:service service-name
                                                               :role role-name}})]
    (if error (throw error) nil)
    (if (or (< status 200) (<= 300 status))
      (throw (Exception. (str "Error. Status is " status "." body)))
      nil)
    (map Host
         (-> body (json/read-str :key-fn keyword) :hosts))))


(defn retire-host
  "Retire the host."
  [apikey host-id]
  (let [{:keys [status body error]} @(http/post (str api-endpoint
                                                     "/api/v0/hosts/" host-id "/retire")
                                                {:headers {"Content-Type" "application/json"
                                                           "X-Api-Key" apikey}})]
    (if error (throw error) nil)
    (if (or (< status 200) (<= 300 status))
      (throw (Exception. (str "Error. Status is " status "." body)))
      nil)
    :ok))
