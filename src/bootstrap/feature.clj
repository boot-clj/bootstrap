(ns bootstrap.feature
  (:require [clojure.spec.alpha :as spec]))

;; Feature Helper Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- feature-url [code]
  (format "https://github.com/boot-clj/boot/wiki/Boot-Features#%s" (name code)))

(defn- feature-data [config feature]
  (spec/explain-data ::feature-gate [feature config]))

(defn- feature-ex [feature error data]
  (ex-info (format "%s See %s" error (feature-url (name feature))) data))

(defn- feature-dispatch [[k v]] k)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Feature Specs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(spec/def ::any any?)

(def prop-true  #{true "true" "yes" "1"})
(def prop-false #{false "false" "no" "0"})

(spec/def ::root-user
  (and #(= "root" (:user-name %))
       #(prop-true (:boot-as-root %))))

(spec/def ::other-user
  #(not= "root" (:user-name %)))

(spec/def ::root-or-other
  (spec/tuple keyword? (spec/or :root-user  ::root-user
                                :other-user ::other-user)))

(spec/def ::auto-pin
  (spec/tuple keyword? #(:boot-version-pin %)))

(spec/def ::offline #(:offline %))

(spec/def ::online  #(not (:offline %)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Feature Multimethod ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti multi-feature feature-dispatch :default ::default)

(defmethod multi-feature ::default [_] (spec/get-spec ::any))

(defmethod multi-feature ::allow-root [_] (spec/get-spec ::root-or-other))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Feature Gate ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(spec/def ::feature-gate (spec/multi-spec multi-feature feature-dispatch))

(spec/check-asserts true) ;; turn on spec asserts

(defn assert-feature [data spec msg]
  (let [feat (spec/conform ::feature-gate [spec data])]
    (if-not (= feat ::spec/invalid) data
      (let [exdata (feature-data data spec)]
        (when-not (= msg ::skip)
          (throw (feature-ex spec msg exdata)))))))

(defn reduce-feature [config feature]
  (reduce-kv assert-feature config feature))

(defmacro when-feature [config feature & body]
  `(when (reduce-feature ~config ~feature) ~@body))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
