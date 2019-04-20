(ns main.logic.task)

;; 定义下面这个函数，最纠结的是不了解chan的机制。不过也大概弄懂了。
;; 其次是该函数以何种方式调用呢？以及该如何结束？
;; 可以先以>!方式调用吧，然后传入消费函数，对结果进行计算。
;; 我总是担忧用不上这个函数用不上的问题，还是先别使用这个函数吧。

#_(def adata (eduction aaa [1]))
#_(defn comp-async [& forms]
    (let [achans (repeatedly (count forms) (a/chan 5))]
      (for [i (- (count forms) 1)]
        (a/pipeline 3 (nth achans (+ i 1)) (nth forms i) (nth achans i)))))


#_(def films-list-chan (a/chan 5))
#_(def films-detail-chan (a/chan 5))
#_(def in-chan (a/chan 5))

#_(a/pipeline 4
            films-list-chan
            (comp (map get-films-list-html)
                  (mapcat parse-films-url))
            in-chan)


#_(a/pipeline 4 films-detail-chan (map get-films-detail) films-list-chan)

#_(a/>!! in-chan 2)
#_(a/>!! in-chan 3)
#_(a/>!! in-chan 4)

#_(def ddd (a/<!! films-detail-chan))

#_(a/go-loop []
  (a/<! films-detail-chan)
  (recur))
