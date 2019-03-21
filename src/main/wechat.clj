(ns main.wechat
  (:require [main.app :refer [env]]
            [ring.util.codec :as codec]))

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
  ())


