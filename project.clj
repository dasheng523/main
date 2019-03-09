(defproject main "0.1.0-SNAPSHOT"
  :description "爬虫版本"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [mount "0.1.12"]    ; 状态管理
                 [cprop "0.1.13"]    ; 配置
                 [org.clojure/tools.logging "0.4.1"]    ; 日志
                 [gorillalabs/neo4j-clj "2.0.1"] ; Neo4j
                 ]
  :main ^:skip-aot main.core
  :target-path "target/%s"
  :plugins []
  :profiles
  {:uberjar {:aot :all}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev {:jvm-opts ["-Dconf=config.edn"]
                 :resource-paths ["env/dev/resources"]
                 :source-paths ["env/dev/clj"]}
   :project/test {:jvm-opts ["-Dconf=config.edn"]
                  :resource-paths ["env/test/resources"]
                  :source-paths ["env/test/clj"]}
   :profiles/dev {}
   :profiles/test {}
   })
