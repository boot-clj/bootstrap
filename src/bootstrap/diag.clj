(ns bootstrap.diag
  (:import [java.net InetAddress]))

(defn ping
  ([ip] (ping ip 1000))
  ([ip timeout] (.isReachable (InetAddress/getByAddress ip) timeout)))
