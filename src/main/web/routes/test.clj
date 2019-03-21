(ns main.web.routes.test
  (:require  [compojure.core :as compojure :refer [GET POST wrap-routes routes]]
             [compojure.route :as route]
             [net.cgrand.enlive-html :as html]))


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
  (apply str (main-template "/hello")))



(defn error-page
  [req]
  (throw (Exception. "test exception")))

(def web-routes
  (compojure/routes
   (GET "/hello" [] hello-world-handler)
   (GET "/index" [] index-page)
   (GET "/error" [] error-page)
   (route/not-found "404")))

