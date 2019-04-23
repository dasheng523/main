(ns main.logic.githook
  (:require [clojure.java.shell :refer [sh]]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]))


(defn parse-data
  "将web请求解析成对应数据格式"
  [req]
  {:event (-> req :headers :X-gogs-event)
   :signature  (-> req :headers :x-gogs-signature)
   :delivery  (-> req :headers :x-gogs-delivery)
   :data (-> req :json-params)})

(defn execute-script
  "执行脚本"
  [script-path]
  (sh script-path))

(defn handle-push
  "处理Push的请求"
  [data]
  (when (= "refs/heads/master" (get (-> data :data) "ref"))
    (log/info "部署项目...")
    (log/info (execute-script "/www/script/deploy.sh"))
    true))



#_(-> push-data :data)
#_(get (-> push-data :data) "ref")
#_(handle-push push-data)

