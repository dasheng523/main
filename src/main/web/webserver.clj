(ns main.web.webserver
  (:require
   [compojure.core :refer [routes wrap-routes]]
   [mount.core :refer [defstate] :as mount]
   [clojure.tools.logging :as log]
   [org.httpkit.server :as server]
   [main.config :refer [env]]
   [main.web.routes.test :as route-test]
   [main.web.middleware :as middleware]
   ))

;; 不能热部署，有点遗憾。不过在ring里面有个插件可以做到，但我并没有使用ring服务器。影响不大，暂时搁置吧.


;; 可能有不同的服务使用不同的中间件，如果处理这个呢？
(def main-handler
  (->
   (routes
    (-> route-test/web-routes
        (wrap-routes middleware/wrap-base)))))


(defstate web-server
  :start
  (do (log/info "正在启动web-server...")
      (server/run-server
       main-handler
       {:port (:web-server-port env)}))
  :stop
  (do (log/info "关闭web-server...")
      (web-server :timeout 500)))
