(ns easy-bake-service.endpoint-schema.api-spec
  (:require
    [speclj.core :refer :all]
    [easy-bake-service.endpoint-schema.api :refer :all]
    [easy-bake-service.fixtures.arbitrary-namespace :refer [some-validator] :as ab]))

(defn test-validation-fn
  "This is a description of a validator function"
  [value])

(describe "Endpoint Documentation API"
  (it "defines an endpoint"
    (should=
      {:name "Test Endpoint" :description "It is just a test bro. I refuse to describe myself."}
      (endpoint "Test Endpoint" "It is just a test bro. I refuse to describe myself.")))

  (context "request"
    (it "describes the request"
      (should=
        {:query-string {} :body {}}
        (:request (endpoint "" "" (request)))))

    (it "describes expected query-string data"
      (should=
        {}
        (:query-string (:request (endpoint "" "" (request (query-string))))))))

  (context "basic types"
    (it "documents a string value"
      (should=
        {:type "string" :name "Name of parameter" :validators []}
        (string "Name of parameter")))

    (it "specifies any validations to be run on the incoming value"
      (should=
        {:type "string" :name "Name of parameter" :validators [{:description "This is a description of a validator function" :function "easy-bake-service.endpoint-schema.api-spec/test-validation-fn"}]}
        (string "Name of parameter" [test-validation-fn])))

    (it "the validation functions can be in any namespace if they are refered in"
      (should=
        {:type "string" :name "Name of parameter" :validators [{:description "some docstring" :function "easy-bake-service.fixtures.arbitrary-namespace/some-validator"}]}
        (string "Name of parameter" [some-validator])))

    (it "the validation functions can be in any namespace if they are refered to by alias"
      (should=
        {:type "string" :name "Name of parameter" :validators [{:description "some other docstring" :function "easy-bake-service.fixtures.arbitrary-namespace/some-other-validator"}]}
        (string "Name of parameter" [ab/some-other-validator])))))
