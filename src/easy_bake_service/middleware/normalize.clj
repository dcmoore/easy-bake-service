(ns easy-bake-service.middleware.normalize
  (:require
    [easy-bake-service.json.parser       :refer [clj-map->json-str json-str->clj-map valid-json-format]]
    [easy-bake-service.xml.parser        :refer [hiccup->xml-str xml-str->hiccup]]
    [easy-bake-service.xml.value-fetcher :refer [tag-extractor]]
    [camel-snake-kebab.core                   :as csk]
    [clojure.walk                        :as walk]
    [ring.util.codec                     :as ring]))


(defn- query-string [request]
  (ring/form-decode (or (:query-string request) "") "utf-8"))

(defn- normalize-hash [hash-to-normalize case-transformer]
  (let [normalize-pair (fn [[k v]] [(keyword (case-transformer k)) v])]
    (walk/postwalk
      #(if (map? %)
        (into {} (map normalize-pair %))
        %)
      hash-to-normalize)))

(defn- normalize-vector [node case-transformer]
  (apply vector
    (concat [(case-transformer (tag-extractor node))]
      (map
        (fn [nested-node]
          (if (vector? nested-node)
            (normalize-vector nested-node case-transformer)
            nested-node))
          (rest node)))))

(defn- normalize-body [request]
  (let [request-body (slurp (or (:body request) (java.io.StringReader. "")))
        content-type (or (:content-type request) "")]
    (if (re-matches #".*application/json.*" content-type)
      (normalize-hash
        (json-str->clj-map request-body)
        csk/->kebab-case)
      (if (re-matches #".*application/xml.*" content-type)
        (normalize-vector
          (xml-str->hiccup request-body)
          csk/->kebab-case)
        (:body request)))))

(defn json-write [response]
    (let [body (normalize-hash (:body response) csk/->camelCase)]
    (if (valid-json-format body)
    (assoc response :body (clj-map->json-str body))
    (throw (Exception. "Invalid JSON in response body")))))

(defn xml-write [response]
  (assoc response :body (hiccup->xml-str (normalize-vector (:body response) csk/->kebab-case))))

(defn- modified-response [response]
  (case (get (:headers response) "Content-Type")
    "application/json" (json-write response)
    "application/xml" (xml-write response)
    response))

(defn wrap-normalize [handler]
  (fn [request]
    (let [normalized-query-params (normalize-hash
                                    (or (:query-params request) (query-string request))
                                    csk/->kebab-case)
          normalized-body (normalize-body request)
          modified-request (assoc request :query-params normalized-query-params :body normalized-body)]
      (modified-response (handler modified-request)))))
