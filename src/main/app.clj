(ns main.app
  (:require [cprop.core :refer [load-config]]
            [mount.core :refer [defstate] :as mount]
            [clojure.tools.logging :as log]
            [org.httpkit.server :as server]
            [neo4j-clj.core :as db]
            [main.web.webserver :refer [main-handler]]
            ))

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

(defstate web-server
  :start
  (do (log/info "正在启动web-server...")
      (server/run-server
       main-handler
       {:port (:web-server-port env)}))
  :stop
  (do (log/info "关闭web-server...")
      (web-server :timeout 500)))


(defn start-app []
  (mount/start))


(defn stop-app []
  (mount/stop))


