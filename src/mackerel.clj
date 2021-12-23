(ns mackerel
  "Mackerel API client."
  (:require
    [clojure.data.json :as json]
    [clojure.string :as str]
    [org.httpkit.client :as http]))


(defn GceHost
  [{:keys [id meta name]}]
  {:id id
   :meta {:cloud {:provider "gce"
                  :metadata {:instance-id (-> meta :cloud :metadata :instance-id)}}}
   :name name})


(defn Host
  [{:keys [id meta name] :as host}]
  (cond (= "gce" (-> meta :cloud :provider)) (GceHost host)
        :else {:id id
               :name name}))


(def api-endpoint "https://api.mackerelio.com")


(defn list-hosts-in-role
  "List hosts in the role."
  [apikey role-fullname]
  (let [[service-name role-name] (str/split role-fullname #" *: *")
        {:keys [status body error]} @(http/get (str api-endpoint "/api/v0/hosts")
                                               {:headers {"X-Api-Key" apikey}
                                                :query-params {:service service-name
                                                               :role role-name}})]
    (if error (throw error) nil)
    (if (or (< status 200) (<= 300 status))
      (throw (Exception. (str "Error. Status is " status ". " body)))
      nil)
    (map GceHost
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
      (throw (Exception. (str "Error. Status is " status ". " body)))
      nil)
    :ok))
