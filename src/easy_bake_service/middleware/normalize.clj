(ns easy-bake-service.middleware.normalize
  (:require
    [camel-snake-kebab  :as csk]
    [clojure.data.json  :as json]
    [clojure.data.xml   :as xml]
    [clojure.string     :as string]
    [clojure.walk       :as walk]
    [ring.util.codec    :as ring]))


(defn- format-attributes [attributes]
  (when attributes
    (into {} (for [[k v] attributes] [(csk/->kebab-case k) v]))))

(defn- empty-when-nil [x]
  (if (nil? x) "" x))

(declare format-full-node)

(defn- format-node [node]
  (cond
   (string? node) (format "\"%s\"" (.trim node))
   (nil? node) nil
   :else (format-full-node node)))

(defn- format-full-node [node]
  (format "[%s %s %s]\n"
          (csk/->kebab-case (:tag node))
          (empty-when-nil (format-attributes (:attrs node)))
          (string/join " " (map format-node (:content node)))))

(defn- transform-str [string]
  (->> string
       java.io.StringReader.
       xml/parse
       format-node
       read-string))

(defn- normalize-hash [hash-to-normalize case-transformer]
  (let [normalize-pair (fn [[k v]] [(keyword (case-transformer k)) v])]
    (walk/postwalk
      #(if (map? %)
        (into {} (map normalize-pair %))
        %)
      hash-to-normalize)))

(defn- normalize-body [request]
  (let [request-body (slurp (or (:body request) (java.io.StringReader. "")))]
    (case (:content-type request)
      "application/json" (normalize-hash
                           (json/read-str request-body)
                           csk/->kebab-case)
      "application/xml" (transform-str request-body)
      (:body request))))

(defn- query-string [request]
  (ring/form-decode (or (:query-string request) "") "utf-8"))

(defn- hiccup->xml [hiccup]
  (if (vector? (last hiccup))
    (xml/element (first hiccup) (second hiccup) (map #(hiccup->xml %) (drop 2 hiccup)))
    (xml/element (first hiccup) (second hiccup) (last hiccup))))

(defn- modified-response [response]
  (case (get (:headers response) "Content-Type")
    "application/json" (when (map? (:body response))
                         (assoc response :body (json/write-str (normalize-hash (:body response) csk/->camelCase))))
    "application/xml" (when (vector? (:body response))
                        (assoc response :body (xml/emit-str (hiccup->xml (:body response)))))
    response))

(defn wrap-normalize [handler]
  (fn [request]
    (let [normalized-query-params (normalize-hash
                                    (or (:query-params request) (query-string request))
                                    csk/->kebab-case)
          normalized-body (normalize-body request)
          modified-request (assoc request :query-params normalized-query-params :body normalized-body)]
      (modified-response (handler modified-request)))))
