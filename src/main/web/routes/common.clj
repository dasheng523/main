
(ns main.web.routes.common
  (:require  [compojure.core :as compojure :refer [GET POST wrap-routes routes]]
             [compojure.route :as route]
             [clojure.tools.logging :as log]
             [net.cgrand.enlive-html :as html]
             [ring.util.response :as resp]
             [cheshire.core :as json]
             [ring.util.http-response :refer :all]
             [main.wechat :as wechat]
             [main.common.func :as func]
             [main.logic.githook :as githook]
             [main.web.middleware :as middleware]))

;; 放一些常规的web服务
(html/deftemplate main-template "templates/index.html"
  [url]
  [:body html/any-node] (html/replace-vars {:url url}))


(defn index-page
  [req]
  (-> (func/fill-url "/wechat")
      (wechat/create-auth-url)
      (main-template)
      (->> (apply str))))

(defn wechat-page
  [{:keys [query-params]}]
  (log/info query-params)
  (resp/response (wechat/get-access-token (get query-params "code"))))

(defn gogs-hook
  [req]
  (githook/handle-push req)
  (ok "ok"))

(def web-routes
  (-> (compojure/routes
       (POST "/gogs-hook" [] gogs-hook))
      (wrap-routes middleware/wrap-base)))

