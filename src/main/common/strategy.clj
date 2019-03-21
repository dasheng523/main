(ns main.common.strategy
  (:require [perseverance.core :as p]))


(defn retrydo
  "抛异常重试，重试max-times次，每次间隔interval毫秒"
  [max-times interval]
  (fn [dofn]
    (fn [& args]
      (p/retry {:strategy (p/constant-retry-strategy interval max-times)}
        (p/retriable {}
          (apply dofn args))))))

