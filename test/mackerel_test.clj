(ns mackerel-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [mackerel]))


(deftest Host-test
  (testing "Create a Host map"
    (is (= {:id "EXAMPLEID"
            :name "example-name"}
           (mackerel/Host {:id "EXAMPLEID"
                           :meta {}
                           :name "example-name"}))))
  (testing "Create a GceHost map when the .cloud.provider is gce"
    (is (= "gce"
           (-> {:id "EXAMPLEID"
                :meta {:cloud {:provider "gce"}}
                :name "example-name"}
               mackerel/Host
               :meta :cloud :provider)))))


(deftest GceHost-test
  (testing "Create a GceHost map"
    (is (= {:id "EXAMPLEID"
            :meta {:cloud {:provider "gce"
                           :metadata {:instance-id "42"}}}
            :name "example-name"}
           (mackerel/GceHost {:id "EXAMPLEID"
                              :meta {:cloud {:provider "gce"
                                             :metadata {:instance-id "42"}}}
                              :name "example-name"})))))
