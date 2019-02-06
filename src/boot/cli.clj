(ns boot.cli
  (:require [clojure.tools.cli :as cli]))

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

(defn parse-opts [args]
  (cli/parse-opts args defaults))
