(ns main.web.middleware
  (:require [clojure.tools.logging :as log]
            [ring.middleware.resource :refer :all]
            [ring.middleware.content-type :refer :all]
            [ring.middleware.not-modified :refer :all]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            ))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t)
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body {:msg "500"}}))))

(defn wrap-reset-body [handler]
  (fn [req]
    (let [body (-> req :body)]
      (.reset body)
      (handler req))))


(defn wrap-base [handler]
  (-> handler
      wrap-internal-error
      wrap-json-response
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)
      wrap-params
      wrap-keyword-params
      wrap-json-params
      wrap-reset-body
      wrap-cookies))
