(ns main.web.routes.api
  (:require [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [ring.util.http-response :refer :all]))


(s/defschema Pizza
  {:name s/Str
   (s/optional-key :description) s/Str
   :size (s/enum :L :M :S)
   :origin {:country (s/enum :FI :PO)
            :city s/Str}})


(def api-routes
  (api
   {:swagger
    {:ui "/api-docs"
     :spec "/swagger.json"
     :data {:info {:title "Sample API"
                   :description "Compojure Api example"}
            :tags [{:name "api", :description "some apis"}]
            :consumes ["application/json"]
            :produces ["application/json"]}}}

   (context "/api" []
     :tags ["api"]

     (GET "/plus" []
       :return {:result Long}
       :query-params [x :- Long, y :- Long]
       :summary "adds two numbers together"
       (ok {:result (+ x y)}))

     (POST "/echo" []
       :return Pizza
       :body [pizza Pizza]
       :summary "echoes a Pizza"
       (ok pizza)))))



(def git-routes
  (api
   {:swagger
    {:ui "/git-docs"
     :spec "/git.json"
     :data {:info {:title "git钩子接口"
                   :description "用于接收git平台接口的接口"}
            :tags [{:name "gogs", :description "gogs hook"}]
            :consumes ["application/json"]
            :produces ["application/json"]}}}

   (context "/gogs" []
     :tags ["gogs"]

     (POST "/push-hook" []
       :return Boolean
       :body [pizza Pizza]
       :summary "echoes a Pizza"
       (ok pizza)))))
