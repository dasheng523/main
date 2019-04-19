(ns main.app
  (:require [main.config]
            [main.web.webserver]
            [main.db]
            [mount.core :refer [defstate] :as mount]))



(defn start-app []
  (mount/start))


(defn stop-app []
  (mount/stop))


