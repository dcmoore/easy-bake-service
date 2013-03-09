(ns easy-bake-service.middleware.normalize-json
  (:use [clojure.data.json :as json]))

(defn- dasherize [camel-cased-string]
  (keyword
    (.toLowerCase
      (clojure.string/replace camel-cased-string #"[A-Z]" #(str "-" %1)))))

(defn- keyify [json-data]
  (into {} (for [[k v] json-data]
    [(dasherize k) v])))

(defn- any-keys-with-spaces? [json-data]
  (not (empty?
    (filter #(re-matches #".*\p{Space}.*" (first %1)) json-data))))

(defn- errors [json-data]
  (if (any-keys-with-spaces? json-data)
    "You can't have any spaces in your JSON keys"))

(defn- contains-json? [request]
  (= "application/json" (get (:headers request) "content-type")))

(defn wrap-json [handler]
  (fn [request]
    (if (contains-json? request)
      (let [json-data (json/read-str (:body request))]
        (if-let [errors (errors json-data)]
          {:status 400 :body errors :headers {}}
          (handler (assoc request :body (keyify json-data)))))
      (handler request))))
