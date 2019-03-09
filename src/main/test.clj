(ns main.test
  (:require  [clojure.test :as t]
             [neo4j-clj.core :as db :refer [defquery]]
             [main.app :refer [env neo-conn]]))

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
