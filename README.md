# 一些乱七八糟的代码

# 编译

```
lein uberjar
```

# 运行

```
java -cp target/uberjar/main-0.1.0-SNAPSHOT-standalone.jar -Dconf=env/dev/resources/config.edn clojure.main -m main.core
```
