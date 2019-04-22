(ns main.logic.githook
  (:require [clojure.java.shell :refer [sh]]
            [clojure.tools.logging :as log]))


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
  [req]
  (let [data (parse-data req)]
    (when (= "refs/heads/master" (-> data :data :ref))
      (log/info "部署项目...")
      (log/info (execute-script "/www/script/deploy.sh")))))
