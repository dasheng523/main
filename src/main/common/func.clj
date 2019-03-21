(ns main.common.func
  (:require [slingshot.slingshot :refer [throw+ try+]]
            [clojure.string :as str]
            [main.config :refer [env]]))



(defn all-trim
  "将字符串中所有空白字符去掉"
  [s]
  (str/join " " (remove empty? (str/split (str/trim s) #"\s"))))



(defn fill-url
  "补充域名前缀给URL"
  [url]
  (str (:domain env) url))
