(ns boot.properties
  "Boot .properties file management."
  (:require [clojure.java.io :as io]
            [clojure.string  :as s])
  (:import [java.io File]
           [java.util Properties]))

(defn- normalize-key [index]
  (-> index
    (s/lower-case)
    (s/replace "_" "-")
    (s/replace "." "-")
    (keyword)))

;; TODO: normalize more types of input
;; TODO: convert string props (true/false, yes/no, 1/0) to bool
(defn- normalize-val [value]
  (if-not (string? value) value
    (s/replace value "~" (System/getProperty "user.home"))))

(defn normalize-map [m]
  (reduce #(assoc %1 (normalize-key (key %2)) (normalize-val (val %2))) {} m))

(defn properties->map
  ([p] (properties->map p normalize-key))
  ([p normalizek] (properties->map p normalizek normalize-val))
  ([p normalizek normalizev]
   (reduce #(assoc %1 (normalizek %2) (normalizev (.getProperty ^Properties p %2))) {} (.keySet ^Properties p))))

(defn map->properties
  ([m] (map->properties m name))
  ([m normalizek] (map->properties m normalizek identity))
  ([m normalizek normalizev] (map->properties (Properties.) m normalizek normalizev))
  ([p m normalizek normalizev]
   (reduce-kv #(doto ^Properties %1 (.setProperty (normalizek %2) (normalizev %3))) p m)))

(defn load-properties
  ([file] (load-properties (Properties.) file))
  ([p file] (if-not (.exists ^File file) (Properties.)
              (with-open [r (io/reader file)]
                (doto ^Properties p (.load r))))))

(defn- pretty-key [index]
  (-> index
    (name)
    (s/upper-case)
    (s/replace "-" "_")))

(defn store-properties
  ([file contents] (store-properties (Properties.) file contents))
  ([p file contents]
   (with-open [w (io/writer file)]
     (when-not (.exists ^File file) (doto file (io/make-parents)))
     (doto ^Properties (map->properties p contents pretty-key identity)
       (.store w "boot.properties")))))

(defn properties-file
  ([file] (properties->map (load-properties file) normalize-key normalize-val))
  ([parent file]  (properties-file (io/file parent file))))
