(ns bootstrap.config
  (:require [boot.properties :as props]))

;; Boot Config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- default-config []
  (props/normalize-map
    {"BOOT_AS_ROOT"         false
     "BOOT_CLOJURE_VERSION" "1.10.0"
     "BOOT_CLOJURE_NAME"    "org.clojure/clojure"
     "BOOT_HOME"            "~/.boot"
     "BOOT_FILE"            "build.boot"
     "BOOT_LOCAL_REPO"      "~/.m2/repository"
     "BOOT_VERSION"         "latest"
     "BOOT_VERSION_PIN"     true
     "BOOT_COLOR"           true}))

(defn work-dir []
  (System/getProperty "user.dir"))

(defn user-home []
  (System/getProperty "user.home"))

(defn environment []
  (props/normalize-map (System/getenv)))

(defn properties []
  (props/properties->map (System/getProperties)))

(defn project []
  (props/properties-file (work-dir) "boot.properties"))

(defn boot-dir []
  (->>
    (:boot-home (default-config))
    (:boot-home (project))
    (:boot-home (environment))
    (:boot-home (properties))))

(defn global []
  (props/properties-file (boot-dir) "boot.properties"))

(defn config []
  (merge
    (default-config)
    (global)
    (project)
    (environment)
    (properties)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
