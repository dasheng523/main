(ns main.learn-some
  (:require #_[main.learn :as sut]
            [clojure.test :as t]
            [neo4j-clj.core :as db :refer [defquery]]
            [perseverance.core :as p]
            [clojure.core.async :refer [>! <! >!! <!! chan] :as a]))

;; 记得使用with-open，因为session是一次性的，需要进行回收with-open。
;; 本来想抽出来的，但是就是考虑到defquery可能需要执行多个命令，所以决定不做。

(defquery test-query
  "CREATE (u:user $user)")

(defquery get-all-users
  "MATCH (u:user) RETURN u as user")

;; Using a session
#_(with-open [session (db/get-session local-db)]
  (create-user session {:user {:firstName "Luke" :lastName "Skywalker"}}))

;; Using a transaction
#_(db/with-transaction local-db tx
  (get-all-users tx)) ;; => ({:user {:firstName "Luke", :lastName "Skywalker"}}))




;; 重试测试案例
;; Fake function that returns a list of files but fails the first three times.
(let [cnt (atom 0)]
  (defn list-s3-files []
    (when (< @cnt 3)
      (swap! cnt inc)
      (throw (RuntimeException. "Failed to connect to S3.")))
    (range 10)))

;; Fake function that imitates downloading a file with 50/50 probability.
(defn download-one-file [x]
  (if (> (rand) 0.5)
    (println (format "File #%d downloaded." x))
    (throw (java.io.IOException. "Failed to download a file."))))

;; Let's wrap the previous function in retriable.
(defn download-one-file-safe [x]
  (p/retriable {} (download-one-file x)))

;; Now to a function that downloads all files.
(defn download-all-files []
  (let [files (p/retriable {:catch [RuntimeException]
                            :tag ::list-files}
                (list-s3-files))]
    (mapv download-one-file-safe files)))

#_(download-all-files)
#_(p/retry {} (download-all-files))


;; 一些常用的需要记住的函数
; 随机取数组
(random-sample 0.1 [1 2 3 4 5])

;; 管道,并行输出一些内容 这个函数异常好用
#_(a/pipeline 4 out xform (a/to-chan items))



;; pipelines 提供普通异步模型
;; fold 提供分而治之的模型


;; transduce 应用
(def xf
  (comp
   (filter odd?)
   (map inc)
   (take 5)))

(transduce xf + (range 5))
(eduction xf (range 5))
(into [] xf (range 1000))
(sequence xf (range 1000))

(def inc-and-filter (comp (map inc) (filter odd?)))
(def special+ (inc-and-filter +))
(special+ 1 2)
;; 4


#_(defn interleave-xform
  [coll]
  (fn [rf]
    (let [fillers (volatile! (seq coll))]
      (fn
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (if-let [[filler] @fillers]
           (let [step (rf result input)]
             (if (reduced? step)
               step
               (do
                 (vswap! fillers next)
                 (rf step filler))))
           (reduced result)))))))



;; pipeline并行输出一些内容 这个函数异常好用
(def xform (comp (filter odd?) (map inc)))
(defn process [items]
  (let [out (a/chan (a/buffer 100))]
    (a/pipeline 4 out xform (a/to-chan items))
    (<!! (a/reduce conj [] out))))
(process (range 10))

;; 通过(reduced result) 可以终止后续的遍历,详情可以看take-while的实现

;; 在transducers里保持状态是非常正常的，大概有一半的标准transducers都有状态
;; 如果transduce有状态，就用volatile!


;; chan
#_(a/pipeline 4 bc (map inc) ac)
;;go-loop 一般配合recur使用，如果其中有状态，一般写到go-loop参数里面

;; 尝试自定义自己的进度管理
;; 一个业务丢进去，指定需要执行的线程数，然后它就不停执行了。
;; 然后接口另一端取出数据，再丢进去，指定线程数，然后它又执行。
;; 就完成了数据抓取工作了。就像一个个车间工序一样。
;; 我所担心的是不够通用，但通不通用需要深度思考才能决定的，不妨先做出第一版功能吧。
;; 只是，数据流是断的。为了接入异步，改变编程模型了。矛盾点应该是这个。
;; 举例：
#_(comp (map fetch-list)
      (mapcat parse-detail)
      (map fetch-detail)
      (map dosave))
;; 这种实现肯定慢得很，而且没办法暂停或重启。
;; 慢是由于后面的函数需要等待前面函数处理完成，没办法暂停是因为没有中间状态。
;; 可以包一层chan来将串行的数据异步化。应该可以解决慢的问题。
;; 在每一层当中再套一圈可暂停化的组件，不就可以控制暂停了吗？
;; 举例：
#_(acomp (asyncable stateable (map fetch-list))
      (mapcat parse-detail)
      (asyncable (fetch-detail))
      (map dosave))
;; 现在先解决可暂停问题
;; 肯定需要有持久化驱动，然后需要保持一个当前需要处理的队列。
;; 麻烦的地方在于在trans里面只有当前结果和当前需要操作的元素，并没有未处理的元素。这可能做不到，队列也许是无穷的，不可获知的。
;; 能做的只是记录已经完成的元素。下一次执行就跳过这些元素。
;; 不过，队列确实是可以持久化的，这只不过是个例罢了。
;; 持久化还有一个方案，那就不管它了，我在源头处暂停启动，并且限制chan的大小，就没有需求要进行持久化了。

;; 既然不能解决持久化，那么先搞定异步吧。现在感觉又退缩了，觉得自己做不成这个，即使做成了也不想用它。我是担心它性能不够好，调整之后发现别的方案更好，然后浪费时间了。但是昨天已经花了不少时间去找方案了，一直没有找到，要么就浪费时间吧。还能提高对transducers的理解。

(def xf
  (comp
   (filter odd?)
   (map inc)
   (take 5)))

(eduction xf [1 2 1 5 3 9 3])

#_(def ac (a/chan 3 (filter odd?)))
#_(def bc (a/chan 3))
#_(def cc (a/chan 3))
#_(a/pipeline 4 bc (map inc) ac)
#_(a/pipeline 4 cc (take 5) bc)

#_(do
  (>!! ac 1)
  (>!! ac 2)
  (>!! ac 1)
  (>!! ac 5)
  (>!! ac 3)
  (>!! ac 9)
  (>!! ac 3))

#_(do
  (println (<!! cc))
  (println (<!! cc))
  (println (<!! cc))
  (println (<!! cc))
  (println (<!! cc)))
