(ns main.web.routes.test
  (:require  [compojure.core :as compojure :refer [GET POST wrap-routes routes]]
             [compojure.route :as route]
             [clojure.tools.logging :as log]
             [net.cgrand.enlive-html :as html]
             [ring.util.response :as resp]
             [main.wechat :as wechat]
             [main.common.func :as func]))


(defn hello-world-handler
  [req]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "hello world!"})

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



(defn error-page
  [req]
  (throw (Exception. "test exception")))

(def web-routes
  (compojure/routes
   (GET "/hello" [] hello-world-handler)
   (GET "/index" [] index-page)
   (GET "/wechat" [] wechat-page)
   (GET "/error" [] error-page)
   (route/not-found "404")))

