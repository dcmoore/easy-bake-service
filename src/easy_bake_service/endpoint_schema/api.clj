(ns easy-bake-service.endpoint-schema.api
  (:require [clojure.string]))

(defn endpoint
  ([name description]
    (endpoint name description {}))
  ([name description other-shit]
    (merge other-shit {:name name :description description})))

(defn request
  ([]
    {:request {:query-string {} :body {}}})
  ([other-shit]
    {:request other-shit}))

(defn query-string []
  {:query-string {}})

(defmacro string
  ([param-name]
    `(string ~param-name []))
  ([param-name validators]
    `{:type "string"
      :name ~param-name
      :validators ~(vec
                    (map
                      (fn [function-sym]
                        (let [fn-var (ns-resolve *ns* function-sym)]
                          {:description (-> fn-var meta :doc)
                           :function (.substring (str fn-var) 2)}))
                      validators))}))
