#!/usr/bin/env bash

lein uberjar
java -cp target/uberjar/main.jar -Dconf=env/prod/resources/config.edn clojure.main -m main.core
