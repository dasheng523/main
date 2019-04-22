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
       (GET "/error" [] error-page)
       (POST "/test-post" [] test-post))
      (wrap-routes middleware/wrap-base)))

