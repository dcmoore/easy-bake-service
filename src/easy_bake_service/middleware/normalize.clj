(ns easy-bake-service.middleware.normalize
  (:require
    [camel-snake-kebab :refer [->kebab-case ->camelCase]]
    [ring.util.codec :refer [form-decode]]
    [clojure.data.json :refer [read-str write-str]]))

(defn- normalize-hash [hash-to-normalize case-transformer]
  (into {}
    (for [[k v] hash-to-normalize]
      [(keyword (case-transformer k)) v])))

(defn- normalize-body [request]
  (if (= "application/json" (:content-type request))
    (normalize-hash
      (read-str (slurp
        (or (:body request) (java.io.StringReader. ""))))
      ->kebab-case)))

(defn- query-string [request]
  (form-decode (or (:query-string request) "") "utf-8"))

(defn- modified-response [response]
  (if (and
        (= "application/json" (get (:headers response) "Content-Type"))
        (map? (:body response)))
    (assoc response :body (write-str (normalize-hash (:body response) ->camelCase)))
    response))


(defn wrap-normalize [handler]
  (fn [request]
    (let [normalized-query-params (normalize-hash
                                    (or (:query-params request) (query-string request))
                                    ->kebab-case)
          normalized-body (normalize-body request)
          modified-request (assoc request :query-params normalized-query-params :body normalized-body)]
      (modified-response (handler modified-request)))))

