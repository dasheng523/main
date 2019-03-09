(ns main.core-test
  (:require [clojure.test :refer :all]
            [main.core :refer :all]
            [cprop.core :refer [load-config]]))


(def env (load-config))

(deftest a-test
  (testing "example, I fail."
    (is (= 1 1))))

(deftest env-test
  (testing "env testing"
    (is (= "88" (env :test-data)))))
