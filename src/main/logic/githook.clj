(ns main.logic.githook
  (:require [clojure.java.shell :refer [sh]]))


#_(sh "ls" "-al")
#_(sh "git")

(defn parse-data
  [req]
  {:event (-> req :headers :X-gogs-event)
   :signature  (-> req :headers :x-gogs-signature)
   :delivery  (-> req :headers :x-gogs-delivery)
   :data (-> req :json-params)})
