(ns main.web.routes.test
  (:require  [compojure.core :as compojure :refer [GET POST wrap-routes routes]]
             [compojure.route :as route]
             [clojure.tools.logging :as log]
             [net.cgrand.enlive-html :as html]
             [ring.util.response :as resp]
             [cheshire.core :as json]
             [ring.util.http-response :refer :all]
             [main.wechat :as wechat]
             [main.common.func :as func]
             [main.web.middleware :as middleware])
  (:import (java.util Base64$Encoder)))


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

(defn test-post
  [req]
  (log/info (json/generate-string
             (-> req
                 (dissoc :route-handler)
                 (dissoc :async-channel)
                 (dissoc :route-middleware)
                 (dissoc :content-type)
                 (dissoc :remote-addr)
                 (dissoc :compojure/route)
                 (dissoc :body))))
  (ok {:dd "test"}))




(def web-routes
  (-> (compojure/routes
       (GET "/hello" [] hello-world-handler)
       (GET "/index" [] index-page)
       (GET "/wechat" [] wechat-page)
       (GET "/error" [] error-page)
       (POST "/test-post" [] test-post))
      (wrap-routes middleware/wrap-base)))

