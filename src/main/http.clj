(ns main.http
  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [perseverance.core :as p]))

(defn create-default-header [host cookies]
  {"User-Agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36"
   "Cookie" cookies
   "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
   "Accept-Encoding" "gzip, deflate, br"
   "Accept-Language" "zh-CN,zh;q=0.9,fr;q=0.8"
   "Cache-Control" "max-age=0"
   "Connection" "keep-alive"
   "Host" host
   "Upgrade-Insecure-Requests" 1})

(defn- read-host-from-url [url]
  (nth (str/split url #"/") 2))

;; 本来想写通用的retry-http的，但是由于使用第三方库是宏，没办法使用comp进行组合。


;; 想设计一个get,post，它有多个线程同时执行，如果线程全部繁忙，则阻塞等待。(可以用pieline)
;; 对于同一个域名，有访问策略，比如每分钟只能访问N次，执行N次休息一次，每次请求需要隔3秒等
;; 也可以给单独域名设置独立的策略。
;; 难点其实就是怎么定义策略，策略应该告诉程序是否执行或者阻塞?

(def nget (create-get ^{:stategy (execute-times-sleep 3)}))




(defn exec []
  (println "doing"))


(defn guard1
  "执行1次，休息3秒"
  []
  (fn [fp]
    (fn []
      (fp)
      (Thread/sleep 3000))))


(defn guard2
  "执行5次，休息30秒"
  []
  (let [times (atom 1)]
    (fn [fp]
      (fn []
        (if (> @times 5)
          (do (Thread/sleep (* 30 1000))
              (reset! times 1))
          (do (fp)
              (swap! times inc)))))))


(defn guard3
  "每15秒内最多只能访问3次"
  []
  (let [times (atom 1)
        start-time (atom (System/currentTimeMillis))]
    (fn [fp]
      (fn []
        (let [lefttime (- (+ @start-time (* 15 1000))
                          (System/currentTimeMillis))]
          (cond (< lefttime 0)
                (do (reset! times 1)
                    (reset! start-time (System/currentTimeMillis)))
                (<= @times 3)
                (do (swap! times inc)
                    (fp))
                (> @times 3)
                (do (Thread/sleep lefttime)
                    (reset! times 1)
                    (reset! start-time (System/currentTimeMillis)))))))))



(def f1 ((guard1) exec))
(def f2 (-> exec
            ((guard1))
            ((guard2))))
(def f3 (-> exec
            ((guard1))
            ((guard2))
            ((guard3))))

;; 调用
(loop []
  (f3)
  (recur))

;; 使用函数嵌套的方式进行组合，以函数为第一公民还真不是吹的，这里让我看到了函数被当成是数据流一样进行组合。
;; 这个模式强无敌
#_(defn [options]
  (fn [next-handler]
    (fn [context])))


