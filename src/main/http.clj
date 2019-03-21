(ns main.http
  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            ))

(defn base-header
  "基础header"
  [host cookies]
  {"User-Agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36"
   "Cookie" cookies
   "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
   "Accept-Encoding" "gzip, deflate, br"
   "Accept-Language" "zh-CN,zh;q=0.9,fr;q=0.8"
   "Cache-Control" "max-age=0"
   "Connection" "keep-alive"
   "Host" host
   "Upgrade-Insecure-Requests" 1})

(defn read-host-from-url
  "从URL中读取域名"
  [url]
  (nth (str/split url #"/") 2))





