(ns main.app
  (:require [cprop.core :refer [load-config]]
            [mount.core :refer [defstate] :as mount]
            [clojure.tools.logging :as log]
            [neo4j-clj.core :as db]))

(defstate env
  :start
  (do
    (log/info "读取配置...")
    (load-config))
  :stop
  (do (log/info "关闭配置...")))


(defstate neo-conn
  :start
  (do (log/info "连接neo4j数据库...")
      (db/connect (:neo4j-url env) (:neo4j-user env) (:neo4j-pass env))))


(defn start-app []
  (mount/start))


(defn stop-app []
  (mount/stop))


