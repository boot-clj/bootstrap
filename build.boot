(set-env!
  :source-paths   #{"src"}
  :dependencies '[[org.clojure/clojure   "1.10.0"]
                  [org.clojure/data.json "0.2.6"]
                  [org.clojure/tools.cli "0.4.1"]
                  [degree9/boot-semver "1.8.0-SNAPSHOT" :scope "test"]])

(require ['degree9.boot-semver :refer :all])

(task-options!
  aot   {:namespace ['bootstrap]}
  pom   {:project 'boot/bootstrap}
  jar   {:main 'bootstrap :file "loader.jar"})

(deftask deps
  "Preload deps into docker container."
  []
  identity)

(deftask build
  "Compile bootstrap classes."
  []
  (comp
    (version)
    (aot)))

(deftask standalone
  "Package bootstrap as a standalone uber-jar."
  []
  (comp
    (uber)
    (jar)
    (target)))

(deftask library
  "Package bootstrap as a library jar."
  []
  (comp
    (build-jar)
    (target)))
