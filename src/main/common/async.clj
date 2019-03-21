(ns main.common.async
  (:require [clojure.core.async :refer [>! <! >!! <!! chan] :as a]))

;; 踩坑了 repeat是完全拷贝的，导致生成一模一样的chan


(defn async->>
  "串联异步数据流，返回in,out两个端口"
  [list & trans]
  (let [chans (for [_ (range (+ 1 (count trans)))] (a/chan))]
    (dotimes [i (count trans)]
      (a/pipeline 3 (nth chans (+ 1 i)) (nth trans i) (nth chans i)))
    {:in (first chans)
     :out (last chans)}))

(defn exec-async
  "传入数据，执行异步数据流"
  [asystream data]
  (doseq [item data]
    (a/go
      (>! (:in asystream) item))))
