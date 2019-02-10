(set-env!
  :source-paths   #{"src"}
  :dependencies '[[org.clojure/clojure   "1.10.0" :scope "provided"]
                  [org.clojure/data.json "0.2.6"  :scope "provided"]
                  [org.clojure/tools.cli "0.4.1"  :scope "provided"]
                  [degree9/boot-semver "1.8.0-SNAPSHOT" :scope "test"]])

(require ['degree9.boot-semver :refer :all]
         ['bootstrap])

(task-options!
  aot   {:namespace ['bootstrap]}
  pom   {:project 'boot/bootstrap
         :description "Bootstrap ."
         :url         "https://github.com/degree9/boot-semver"
         :scm {:url "https://github.com/degree9/boot-semver"}})

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
    (jar :main 'bootstrap :file "bootstrap.uber.jar")
    (target)))

(deftask library
  "Package bootstrap as a library jar."
  []
  (comp
    (build-jar)
    (target)))
