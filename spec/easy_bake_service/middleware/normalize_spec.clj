(ns easy-bake-service.middleware.normalize-spec
  (:use
    [speclj.core]
    [easy-bake-service.middleware.normalize]))


(defn- modified-request [test-request]
  ((wrap-normalize identity) test-request))

(defn- modified-response [test-response]
  ((wrap-normalize (fn [_] test-response)) {}))


(describe "Normalize Middleware"
  (context "request"
    (context "query-params"
      (it "normalizes camelCase parameters"
        (let [request {:query-params {"goodGuy" "Master Splinter"}}]
          (should=
            {:good-guy "Master Splinter"}
            (:query-params (modified-request request)))))

      (it "normalizes snake_case parameters"
        (let [request {:query-params {"good_guy" "Master Splinter"}}]
          (should=
            {:good-guy "Master Splinter"}
            (:query-params (modified-request request))))))

    (context "query-string"
      (it "normalizes camelCase parameters"
        (let [request {:query-string "goodGuy=Master%20Splinter"}]
          (should=
            {:good-guy "Master Splinter"}
            (:query-params (modified-request request)))))

      (it "normalizes snake_case parameters"
        (let [request {:query-string "good_guy=Master%20Splinter"}]
          (should=
            {:good-guy "Master Splinter"}
            (:query-params (modified-request request))))))


    (context "body"
      (it "normalizes camelCase keys"
        (let [request {:content-type "application/json"
                       :body (java.io.StringReader. "{\"jsonKey\":\"json value\"}")}]
          (should=
            {:json-key "json value"}
            (:body (modified-request request)))))

      (it "normalizes snake_case keys"
        (let [request {:content-type "application/json"
                       :body (java.io.StringReader. "{\"json_key\":\"json value\"}")}]
          (should=
            {:json-key "json value"}
            (:body (modified-request request)))))

      (it "normalizes nested keys"
        (let [request {:content-type "application/json"
                       :body (java.io.StringReader. "{\"top_level_key\":{\"nested_key\":\"value\"}}")}]
          (should=
            {:top-level-key {:nested-key "value"}}
            (:body (modified-request request)))))))

  (context "response"
    (context "converts clojure data to a json string"
      (it "sets the keys to camelCase"
        (let [response {:status 200
                        :body {:json-key "json value"}
                        :headers {"Content-Type" "application/json"}}]
          (should=
            "{\"jsonKey\":\"json value\"}"
            (:body (modified-response response)))))

      (it "works for nested keys too"
        (let [response {:status 200
                        :body {:json-key {:nested-key "nested value"}}
                        :headers {"Content-Type" "application/json"}}]
          (should=
            "{\"jsonKey\":{\"nestedKey\":\"nested value\"}}"
            (:body (modified-response response))))))

    (context "doesn't alter the request"
      (it "when the Content-Type is anything other than application/json"
        (let [response {:status 200
                        :body {:json-key "json value"}
                        :headers {}}]
          (should=
            {:json-key "json value"}
            (:body (modified-response response)))))

      (it "when the body isn't a map"
        (let [response {:status 200
                        :body "{:json-key \"json value\"}"
                        :headers {}}]
          (should=
            "{:json-key \"json value\"}"
            (:body (modified-response response))))))))


(run-specs)
