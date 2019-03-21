#!/usr/bin/env bash

lein uberjar
java -cp target/uberjar/main.jar -Dconf=env/dev/resources/config.edn clojure.main -m main.core
