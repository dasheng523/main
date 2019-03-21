(ns main.wechat
  (:require [main.app :refer [env]]
            [main.common.strategy :refer [retrydo]]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [ring.util.codec :as codec]))

(def http-post
  (-> http/post
      ((retrydo 5 500))))

(defn create-auth-url
  "创建认证url,url应为https链接"
  ([url scope state]
   (str "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" (:wx-appid env) "&redirect_uri=" (codec/url-encode url) "&response_type=code&scope=" scope "&state=" state "#wechat_redirect"))
  ([url]
   (create-auth-url url "snsapi_base" "default")))

#_(create-auth-url "http://www.dianduoduo.top/aaa?ff=2")

(defn get-access-token
  "获取access-token"
  [code]
  (let [url (str "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" (:wx-appid env) "&secret=" (:wx-appsecret env) "&code=" code "&grant_type=authorization_code")]
    (-> (http-post url)
        :body
        (json/parse-string true))))


