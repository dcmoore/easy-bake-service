(ns easy-bake-service.xml.parser
  (:require
    [clojure.data.xml  :as xml]
    [camel-snake-kebab.core :as csk]
    [clojure.string    :as string]))

(defn- format-attributes [attributes]
  (when attributes
    (into {} (for [[k v] attributes] [(csk/->kebab-case k) v]))))

(defn- empty-brackets-when-nil [x]
  (if (nil? x) "{}" x))

(declare format-full-node)

(defn- format-node-body [node]
  (cond
   (string? node) (format "\"%s\"" (string/escape node {\" "\\\""}))
   (nil? node) nil
   :else (format-full-node node)))

(defn- format-full-node [node]
  (format "[%s %s %s]\n"
    (:tag node)
    (empty-brackets-when-nil (format-attributes (:attrs node)))
    (string/join " " (map format-node-body (:content node)))))

(defn- build-xml-element-tree [hiccup]
  (if (vector? (last hiccup))
    (xml/element (first hiccup) (second hiccup) (map #(build-xml-element-tree %) (drop 2 hiccup)))
    (xml/element (first hiccup) (second hiccup) (last hiccup))))


(defn hiccup->xml-str [hiccup]
  (xml/emit-str (build-xml-element-tree hiccup)))

(defn xml-str->hiccup [xml-str]
  (->> xml-str
       java.io.StringReader.
       xml/parse
       format-full-node
       read-string))
