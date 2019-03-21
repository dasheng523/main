(ns main.app
  (:require [main.config]
            [main.web.webserver]
            [mount.core :refer [defstate] :as mount]))

#_(defstate neo-conn
  :start
  (do (log/info "连接neo4j数据库...")
      (db/connect (:neo4j-url env) (:neo4j-user env) (:neo4j-pass env))))


(defn start-app []
  (mount/start))


(defn stop-app []
  (mount/stop))


