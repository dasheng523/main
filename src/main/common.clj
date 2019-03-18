(ns main.common
  (:require [slingshot.slingshot :refer [throw+ try+]]
            [clojure.string :as str]))



(defn all-trim
  "将字符串中所有空白字符去掉"
  [s]
  (str/join " " (remove empty? (str/split (str/trim s) #"\s"))))

