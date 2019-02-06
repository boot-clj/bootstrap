(ns bootstrap
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [bootstrap.config :as conf]
            [boot.cli :as cli]
            [boot.properties :as props]
            [bootstrap.feature :as feature])
  (:import  [java.io File]
            [java.util List])
  (:gen-class))

;; GraalVM ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(set! *warn-on-reflection* true) ;; warnings will result in runtime crashes

(def boot-url "https://boot-clj.com")

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
  (let [url     (version-url version)
        jar     (version-jar version)]
    (when-not (.exists ^File jar)
      (println (format "Downloading %s..." url))
      (download-file url jar))))

(defn- pin-version [version]
  (let [props (io/file (conf/work-dir) "boot.properties")]
    (when (and (.exists ^File (io/file (conf/work-dir) "boot.properties"))
               (not= version (:boot-version (conf/project))))
      (println (format "Pinning BOOT_VERSION to %s..." version))
      (-> (props/load-properties props)
        (props/store-properties props {"BOOT_VERSION" version})))))

(defn- launch-version [version args]
  (let [jar     (.getAbsolutePath ^File (version-jar version))
        command (:boot-java-command (conf/config) "java")
        ^List args (into [command "-jar" jar] args)]
    (.waitFor (.start (.inheritIO (ProcessBuilder. args))))))

(defn- print-version [config]
  (let [kw->env (fn [k] (-> (name k) (str/upper-case) (str/replace "-" "_")))]
    (println (format "%s - the clojure build tool." boot-url))
    (doseq [key [:boot-version :boot-clojure-version :boot-clojure-name]]
      (println (format "%s=%s" (kw->env key) (key config))))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Boot Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn -main [& args]
  ;; GraalVM - needed until graalvm can include within the native image;;;;;;;;;
  (System/setProperty "java.library.path" (str (jvm-home) "/jre/lib/amd64"))
  (let [config  (conf/config)
        version (parse-version (:boot-version config))
        {opts :options :as args} (cli/parse-opts args)]
    (println opts)
    (cond (:version opts) (print-version config)
      :else (feature/when-feature config
              {::feature/allow-root "Boot is refusing to run as user \"root\"."}
              (feature/when-feature opts              ;; download boot to cache
                {::feature/online ::feature/skip}     ;; skip when offline
                (download-version version))
              (feature/when-feature config            ;; pin version to project
                {::feature/auto-pin ::feature/skip}   ;; skip error message
                (pin-version version))
              (launch-version version args)))))       ;; launch jvm cache jar
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
