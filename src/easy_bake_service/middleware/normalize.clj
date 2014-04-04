(ns easy-bake-service.middleware.normalize
  (:require
    [easy-bake-service.json.parser       :refer [clj-map->json-str json-str->clj-map]]
    [easy-bake-service.xml.parser        :refer [hiccup->xml-str xml-str->hiccup]]
    [easy-bake-service.xml.value-fetcher :refer [tag-extractor]]
    [camel-snake-kebab                   :as csk]
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
  (let [request-body (slurp (or (:body request) (java.io.StringReader. "")))]
    (case (:content-type request)
      "application/json" (normalize-hash
                           (json-str->clj-map request-body)
                           csk/->kebab-case)
      "application/xml" (normalize-vector
                           (xml-str->hiccup request-body)
                           csk/->kebab-case)
      (:body request))))

(defn- modified-response [response]
  (case (get (:headers response) "Content-Type")
    "application/json" (assoc response :body (clj-map->json-str (normalize-hash (:body response) csk/->camelCase)))
    "application/xml" (assoc response :body (hiccup->xml-str (normalize-vector (:body response) csk/->kebab-case)))
    response))

(defn wrap-normalize [handler]
  (fn [request]
    (let [normalized-query-params (normalize-hash
                                    (or (:query-params request) (query-string request))
                                    csk/->kebab-case)
          normalized-body (normalize-body request)
          modified-request (assoc request :query-params normalized-query-params :body normalized-body)]
      (modified-response (handler modified-request)))))
