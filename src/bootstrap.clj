(ns bootstrap
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [bootstrap.config :as conf]
            [bootstrap.properties :as props]
            [bootstrap.feature :as feature])
  (:import  [java.io File]
            [java.util List])
  (:gen-class))

;; GraalVM ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(set! *warn-on-reflection* true) ;; warnings will result in runtime crashes

(defn- jvm-home []
  (or (System/getenv "GRAALVM_HOME") (System/getenv "JAVA_HOME")))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Boot Internal Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def boot-url "https://boot-clj.com")

(defn- boot-artifact []
  (json/read-str (slurp "https://clojars.org/api/artifacts/boot") :key-fn keyword))

(defn- latest-release []
  (:latest_release (boot-artifact)))

(defn- latest-version []
  (:latest_version (boot-artifact)))

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

(defn- update-version [opts version]
  (cond (:update opts)          (download-version (latest-release))
        (:update-snapshot opts) (download-version (latest-version))
        :else                   (download-version version)))

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
(def ^:private defaults
  [["-v" "--verbose"
    "Verbose output with increasing noise. (-v Level=1, -vv Level=2, -vvv Level=3)"
    :default 0 :update-fn inc]
   ["-V" "--version"
    "Print boot version info."]
   ["-u" "--update"
    "Update boot to latest release version."]
   ["-U" "--update-snapshot"
    "Update boot to latest snapshot version."]])

(def ^:private features
  {::feature/allow-root   "Boot is refusing to run as \'root\' user."
   ::feature/offline-mode "Boot requires an internet connection but appears to be offline."})

(defn- run-boot [{version :boot-version :as config} {opts :options args :arguments}]
  (if (:version opts) (print-version config)
    (feature/when-feature config features
      (let [{version :boot-version} config]
        (when-not (:offline opts)
          (update-version opts version))
        (when (:boot-version-pin config)
          (pin-version version))
        (launch-version version args)))))

(defn -main [& args]
  ;; GraalVM - needed until graalvm can include within the native image;;;;;;;;;
  (System/setProperty "java.library.path" (str (jvm-home) "/jre/lib/amd64"))
  (run-boot (conf/config) (cli/parse-opts args defaults :in-order true)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
