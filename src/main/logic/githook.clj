(ns main.logic.githook
  (:require [clojure.java.shell :refer [sh]]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [pandect.algo.sha1 :as pan1]
            [pandect.algo.sha256 :as pan]))




(defn parse-data
  "将web请求解析成对应数据格式"
  [req]
  {:event (-> req :headers :X-gogs-event)
   :signature  (-> req :headers :x-gogs-signature)
   :delivery  (-> req :headers :x-gogs-delivery)
   :data (-> req :json-params)})

(defn handle-push
  "处理Push的请求"
  [data]
  (log/info (get (-> data :data) "ref"))
  (when (= "refs/heads/master" (get (-> data :data) "ref"))
    (log/info "部署项目...")
    (log/info (sh "/www/script/deploy.sh"))
    true))


(println (pan1/sha1 "123456"))
(pan/sha256 "1111")
