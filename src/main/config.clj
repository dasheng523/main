(ns main.config
  (:require [mount.core :refer [defstate] :as mount]
            [clojure.tools.logging :as log]
            [cprop.core :refer [load-config]]))

(defstate env
  :start
  (do
    (log/info "读取配置...")
    (load-config))
  :stop
  (do (log/info "关闭配置...")))
