(ns easy-bake-service.json.parser-spec
  (:require
    [easy-bake-service.json.parser :refer :all]
    [speclj.core                   :refer :all]))

(describe "JSON Parser"
  (context "Validating JSON Format"
    (it "returns true for an array"
      (should= true (valid-json-format [1 2 3])))
    (it "returns true for a JSON object"
      (should= true (valid-json-format {:key "value"})))
    (it "returns false for a plain string"
      (should= false (valid-json-format "value")))))
