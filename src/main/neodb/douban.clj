(ns main.neodb.douban
  (:require [neo4j-clj.core :as db :refer [defquery]]
            #_[main.db :refer [neo-conn]]
            [clojure.string :as str]))










;; 想要整一套便捷函数出来，程序员都有减少重复的习惯，抽离是正常的,为了更好的复用，是不会有错的。
;; 但控制系统复杂度，一般靠数据抽象和过程抽象。抽离不一定能解决复杂度问题的。
;; 要思考封装，上层不应知道下层的技术细节。每一层都可以看成是编译器。做到最大的隔离。
;; 写代码的本质其实是将无穷可能变成有限集合。不能扔掉无穷的灵活，也不能丢掉有穷的实用。所以目前我的做法是使用宏来批量定义函数，用数据来限制无穷，同时开放无穷接口。
;; ps. 另外遇到困惑的地方，最好不要强行思考，通过实践去思考问题。

;; 其中最大的改变在于对查询语句进行结构化吧。
;; 如何选择query和程序的结合点？程序根据用户数据，动态生成query。
;; 想明白了，我们写的代码本质上是query成多个片段，然后提供给程序员组合成更简洁更好用的模式。其实，如果query解释器能够提供这些片段的接口，我们也不需要打碎成片段。这就要求程序员必须将query结构化。
;; 其实还是回到了原来，我虽然写的是编译器，但是这个编译器很简单，没多大内容，所以封装成编辑器不太需要。
;; 如何写查询呢？
;; 我现在最纠结的是事务该怎么理解。如果我使用事务，那么刚刚写的create-note变得意义不大。不还是有意义的，如果仅仅只有一条语句，选择他准没错。如果有多条操作，那么意义不大了。
;; 先理一理如何写查询语句的复用吧。直接替换或拼接query？还能怎样，拼接片段咯。除非找到拼接片段的套路和规则，才能自动生成片段。
;; 套路也还是有的，比如对象，查询条件等。这些以后再说吧。现在就缺乏删除，但是删除也有跟属性相关的。
;; 我就是想要这个层里的函数都是不可拆分的，自动成为一个统一事务而不需要手动指定。

(def node-list
  ["person" "movie" "prize"])


;; 创建语句
(def create-sentence
  #(str "CREATE (p:" % " $data)"))


(def neo-conn 1)

(defn simple-query
  "简单查询"
  ([query data]
   (with-open [session (db/get-session neo-conn)]
     (db/execute session query data)))
  ([query]
   (simple-query query {})))

#_(simple-query (delete-sentence "Movie")
                {:data {:title "美丽心灵"}})
#_(simple-query (match-sentence "Movie")
                {:title "美丽心灵"})
#_(simple-query "MATCH (n:Movie) return n")


;; 删除语句,将删除所有的相关节点
;; 这些语句跟直接写query是等价的，难道不需要写这些吗？必须写，我其实是被where条件限制罢了。我得绕开他。
(def delete-by-sentence
  #(str "MATCH (n:" % " $data) DELETE n"))

(def match-by-sentence
  #(str "MATCH (n:" % " {title: $title}) RETURN n"))

(defn- parse-data->str
  "将data转化为字符串"
  [data]
  (if (map? data)
    (->> (for [item data]
           (let [k (name (first item))]
             (str k ": \\$" k)))
         (str/join ", ")
         (#(str "{" % "}")))
    (str data)))


(defn parse-query
  "转换query"
  [query data]
  (reduce (fn [rs item]
            (let [k (name (first item))
                  v (parse-data->str (last item))]
              (str/replace rs (re-pattern (str "<" k ">")) v)))
          query
          data))


#_(parse-query "MATCH (n:$label <where>) return n" {:label 111 :where {:a 66}})

(defn execute-query [query data]
  (simple-query
   (parse-query query data)
   data))

#_(execute-query (str "MATCH (n:<label> <where>) return n") {:label "Person" :where {:a 66}})


;; 现在的大问题就是事务
;; 其实事务大多是一些副作用的语句罢了，跟查询无关。所以最终生成的语句必然是一个列表。批量执行这个列表就能控制事务的最终执行了。
;; 收集这些执行语句，就能逐渐建立一套复杂的事务体系。但这套体系不可以依赖执行语句的返回值。
#_(do-execute1
 (let [a (query (str "MATCH (n:<label> <where>) return n") {:label 111 :where {:a 66}})]
   (if a (query "MATCH (n:<label> <where>) DELETE n" {:label 111 :where {:a 66}}))))

#_(do-execute2
 (let [a (query (str "MATCH (n:Person {title: $title}) return n") {:title "8899"})]
   (if a (delete a))))




#_(build-query {:method "return"
              :label "person"
              :data {:a 66}
              :type "MATCH"})


(defn simple-create-node
  "在neo中创建一个节点"
  [label data]
  (simple-query (str "CREATE (p:" label " $data)")
                {:data data}))



;; 可能是太久没写宏了，忘记宏是编译期执行了。
;; 如果在`之前就进行一些转换操作，一定要记住，宏是编译期执行的。
(defmacro define-create-fns
  "定义一系列创建函数"
  [labels]
  `(doseq [label# ~labels]
     (let [fname# (symbol (str "create-" label#))]
       (def fname# (partial simple-create-node label#)))))


#_(define-create-fns node-list)

