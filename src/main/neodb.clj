(ns main.neodb
  (:require [mount.core :refer [defstate] :as mount]
            [clojure.tools.logging :as log]
            [neo4j-clj.core :as db :refer [defquery]]
            [main.config :refer [env]]))

(defstate neo-conn
    :start
    (do (log/info "连接neo4j数据库...")
        (db/connect (:neo4j-url env) (:neo4j-user env) (:neo4j-pass env))))
