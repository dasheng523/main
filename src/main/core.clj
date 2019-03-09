(ns main.core
  (:gen-class)
  (:require [main.app :refer [start-app]]))

(defn -main
  "程序入口"
  [& args]
  (start-app)
  (println "启动完毕"))
