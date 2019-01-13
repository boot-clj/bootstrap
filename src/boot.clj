(ns boot
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [boot.config :as conf]
            [boot.properties :as props])
  (:import  [java.io File]
            [java.util List])
  (:gen-class))

;; GraalVM ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(set! *warn-on-reflection* true) ;; warnings will result in runtime crashes

(defn- jvm-home []
  (or (System/getenv "GRAALVM_HOME") (System/getenv "JAVA_HOME")))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Boot Internal Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- latest-version []
  (-> "https://clojars.org/api/artifacts/boot"
    (slurp)
    (json/read-str :key-fn keyword)
    (:latest_release)))

(defn parse-version [version]
  (if (= version "latest") (latest-version) version))

(defn- version-url [version]
  (str "https://clojars.org/repo/boot/boot/" version "/boot-" version ".jar"))

(defn- download-file [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn- version-jar [version]
  (let [jar-file (io/file (conf/boot-dir) "cache" "bin" version "boot.jar")]
    (doto jar-file (io/make-parents))))

(defn- download-version [version]
  (let [version (parse-version version)
        url     (version-url version)
        jar     (version-jar version)]
    (when-not (.exists ^File jar)
      (println (format "Downloading %s..." url))
      (download-file url jar))))

(defn- pin-version [version]
  (let [props   (io/file (conf/work-dir) "boot.properties")
        version (parse-version version)]
    (when (and (:boot-version-pin (conf/config))
               (.exists ^File (io/file (conf/work-dir) "boot.properties"))
               (not= version (:boot-version (conf/project))))
      (println (format "Pinning BOOT_VERSION to %s..." version))
      (-> (props/load-properties props)
        (props/store-properties props {"BOOT_VERSION" version})))))

(defn- launch-version [version args]
  (let [jar (.getAbsolutePath ^File (version-jar version))
        command (:boot-java-command (conf/config) "java")
        ^List args (into [command "-jar" jar] args)]
    (.waitFor (.start (.inheritIO (ProcessBuilder. args))))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Boot Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn -main [& args]
  ;; GraalVM - needed until graalvm can include within the native image;;;;;;;;;
  (System/setProperty "java.library.path" (str (jvm-home) "/jre/lib/amd64"))
  (let [version (:boot-version (conf/config))] ;; load config
    (download-version version)                 ;; download boot to cache
    (pin-version version)                      ;; pin version to project
    (launch-version version args)))            ;; launch jvm cache jar
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
