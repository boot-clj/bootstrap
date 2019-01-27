(ns boot.exceptions)

(defn- exception-url [code]
  (format "https://github.com/boot-clj/boot/wiki/Boot-Exceptions#%s" code))

(defn security [msg code]
  (throw (SecurityException. (format "%s See %s" msg (exception-url code)))))
