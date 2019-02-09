(set-env!
  :source-paths   #{"src"}
  :dependencies '[[org.clojure/clojure   "1.10.0"]
                  [org.clojure/data.json "0.2.6"]
                  [org.clojure/tools.cli "0.4.1"]])

(task-options!
  javac {:options ["-Xlint:unchecked"]}
  aot   {:namespace ['bootstrap]}
  pom   {:project 'boot/bootstrap :version "3.0.0-SNAPSHOT"}
  jar   {:main 'bootstrap :file "loader.jar"})

(deftask deps
  "Preload deps into docker container."
  []
  identity)

(deftask build
  "Compile bootstrap classes and package as uber-jar."
  []
  (comp
    (javac)
    (aot)
    (pom)
    (uber)
    (jar)
    (target)))
