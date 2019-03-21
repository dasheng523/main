(ns main.http
  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [perseverance.core :as p]
            ))


;; 想设计一个get,post，它有多个线程同时执行，如果线程全部繁忙，则阻塞等待。(可以用pieline)
;; 这其实就是想要异步罢了。client有提供异步的能力。
;; 想了想，串行异步的实现还是要有的，因为组装起来就确实很方便了。



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



;; 对于同一个域名，有访问策略，比如每分钟只能执行N次，执行N次休息一次，每次执行需要隔3秒等
;; 也可以给单独域名设置独立的策略。
;; 难点其实就是怎么定义策略，策略应该告诉程序是否执行或者阻塞?
;; 先把这些常规的策略写好吧，就写在这文件里吗？如果有别的地方需要，岂不是要引用它？但如果放在别处，该起什么名？strategy?
;; 也就只有个别策略是通用的，其他的可能跟域名,网址,变量有关，通用的就新建文件吧。
;; 等到要写的时候，发现还暂时不需要那么多控制策略，先把串行异步链写好吧。



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
  "每15秒内最多只能执行3次"
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
#_(loop []
  (f3)
  (recur))

;; 使用函数嵌套的方式进行组合，以函数为第一公民还真不是吹的，这里让我看到了函数被当成是数据流一样进行组合。

;; 这个模式强无敌
#_(defn [options]
  (fn [next-handler]
    (fn [context])))
