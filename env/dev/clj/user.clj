(ns user
  (:require [main.app :refer [start-app stop-app]]))


(defn start []
  (start-app))

(defn stop []
  (stop-app))

