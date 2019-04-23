(ns main.githook-test
  (:require  [clojure.test :refer :all]
             [clojure.java.io :as io]
             [cheshire.core :as json]
             [main.logic.githook :as githook]))


(def data {:event "push"
           :signature "49971966a994ee7294b55c985407b6013bf7e7694a897c28b2155b19ecd8523c"
           :delivery "d5c91d8a-641d-43f7-a800-4f5efdd1287e"
           :data (json/parse-string (slurp (io/resource "tests/dd.json")))})

(deftest handle-push-test
  (testing "测试push"
    (is (= "refs/heads/master" (get (-> data :data) "ref")))))
