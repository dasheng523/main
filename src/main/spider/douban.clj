(ns main.spider.douban
  (:require [main.config :refer [env]]
            [main.html-parser :as hparser]
            [main.common.func :as common]
            [main.common.strategy :as strategy]
            [clj-http.client :as http]
            [perseverance.core :as p]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [net.cgrand.enlive-html :as enlive]
            [slingshot.slingshot :refer [throw+ try+]]
            [clojure.core.async :as a]
            [clojure.string :as str]))

;; 引入异常会更好，异常应该是另一条业务链，可以通过捕获来进行很多深度扩展，如果仅仅返回nil，就必须在当前业务进行处理，这一点不是很好。

(defn- create-default-header
  "默认的Http头"
  []
  {"User-Agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36"
   "Cookie" (:douban-cookies env)
   "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
   "Accept-Encoding" "gzip, deflate, br"
   "Accept-Language" "zh-CN,zh;q=0.9,fr;q=0.8"
   "Cache-Control" "max-age=0"
   "Connection" "keep-alive"
   "Upgrade-Insecure-Requests" 1})


;; 我觉得应该在这个函数中定义各种抓取策略，而不是在具体逻辑中掺杂各种调度策略。它应该抽离成单独体系。
(def http-get
  #((-> http/get
        ((strategy/retrydo 5 1000)))
    %
    (create-default-header)))



(defn get-movies-list-html
  "获取电影列表html,从第0页开始"
  [page]
  (some-> (str "https://movie.douban.com/j/new_search_subjects?sort=U&range=0,10&tags=&start=" (* page 20))
          http-get
          :body))


(defn parse-movies-url
  "在电影列表数据中解析出对应的详情url"
  [html]
  (let [data (-> html
                 (json/parse-string true)
                 :data)]
    (if (<= (count data) 0)
      (throw+ {:type :fecth-error :msg "获取电影列表数据异常" :data html}))
    (map :url data)))

;; 先能读详情页吧，然后再分拆出来。只是现在还纠结的是获奖情况，不过这个完全可以先放一放。
;; 另外需不需要将IO操作和解析操作隔离呢？我觉得应该可以隔离，追求性能的最优，也更纯粹了代码。但是这要暴露技术细节到外面，显得没必要。
;; 还有请求过程中如果有预期之外的事情，该如何截取分析。
;; 需不需要接入最新的spec用作参数检测，或者用schema? 用schema吧，spec感觉还是太难用了，不能因为是官方的就强行用spec。用过了确实不好用，定义太多字段了，导致代码很难看。

(defn url-to-id
  "从url提取id"
  [url]
  (last (str/split url #"/")))


(defn get-movies-detail
  "获取电影详情数据"
  [url]
  (let [html (some-> url
                     http-get
                     :body)
        selectors {:firm-info (hparser/select-json #"ld\+json\">([\s\S]+?)</script>")
                   :intro (hparser/select-text [:div#link-report (enlive/attr? :property)])}
        result (hparser/parse html selectors)]
    (if (empty? (some-> result :firm-info :name))
      (throw+ {:type :fecth-error :msg "获取电影详情数据异常" :data html}))
    (assoc (:firm-info result)
           :description
           (:intro result)
           :id
           (url-to-id (-> result :firm-info :url)))))

;; test
#_(last (str/split "/subject/3878007/" #"/"))
#_(def film-detail (get-movies-detail "https://movie.douban.com/subject/1292064/"))

(defn parse-awards
  "解析奖项页面数据"
  [html]
  (let [list-selector
        (hparser/select-list
         [:div.result_list :div.channels]
         {:title (hparser/select-text [:h3])
          :items (hparser/select-list
                  [:div.rslt_wrap :li]
                  {:name (comp
                          common/all-trim
                          (hparser/select-text [:div.r_main]))
                   :id (comp
                        url-to-id
                        (hparser/select-href [:div.r_main :a]))
                   :won (comp
                         common/all-trim
                         (hparser/select-text [:div.won]))
                   :dire (comp
                          common/all-trim
                          (hparser/select-text [:div.r_sub]))})})]
    (hparser/parse html {:list list-selector})))


;; test
#_(def hh (:body (http-get "https://movie.douban.com/awards/doubanfilm_annual/4/nominees")))
#_(parse-awards hh)


(defn create-celebrities-url
  "根据电影ID生成所有演职员URL"
  [firm-id]
  (str "https://movie.douban.com/subject/" firm-id "/celebrities"))

(defn parse-celebrities
  "解析电影全部演职员"
  [html]
  (hparser/parse
   html
   {:list (hparser/select-list
           [:div.article :div.list-wrapper]
           {:item-name (hparser/select-text [:h2])
            :person-list
            (hparser/select-list [:ul.celebrities-list :li]
                                 {:name (hparser/select-text [:div.info :span.name :a])
                                  :id (comp url-to-id
                                            (hparser/select-href [:div.info :span.name :a]))})})}))

;; test
#_(def hh (:body (http-get (create-celebrities-url 3878007))))
#_(println (parse-celebrities hh))


(defn create-celebrity-info-url
  "根据演员ID生成URL"
  [celebrity-id]
  (str "https://movie.douban.com/celebrity/" celebrity-id "/"))


(defn parse-celebritie-info
  "解析演员基本资料"
  [html]
  (hparser/parse
   html
   {:name (hparser/select-text [:div#content :h1])
    :intro (comp
            common/all-trim
            (hparser/select-text [:div#intro :div.bd]))
    :attrs (hparser/select-list
            [:div#headline :div.info :li]
            {:val (comp
                   common/all-trim
                   (hparser/select-text [:*]))})}))

#_(def hh (:body (http-get (create-celebrity-info-url "1044702"))))
#_(parse-celebritie-info hh)

(defn create-celebrity-image-url
  "获取演员相册列表地址"
  [celebrity-id]
  (str "https://movie.douban.com/celebrity/" celebrity-id "/photos/"))
