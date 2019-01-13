(set-env!
  :source-paths   #{"src"}
  :dependencies '[[org.clojure/clojure   "1.10.0"]
                  [org.clojure/data.json "0.2.6"]
                  [org.slf4j/slf4j-nop   "1.8.0-beta2" :scope "test"]])

(deftask deps
  "Preload deps into docker container."
  []
  identity)


(deftask build
  "Compile boot-graalvm classes and package as jar."
  []
  (comp
    (javac :options ["-Xlint:unchecked"])
    (aot :namespace ['boot])
    (pom :project 'boot-native :version "3.0.0-SNAPSHOT")
    (uber)
    (jar :main 'boot :file "loader.jar")
    (target)))
