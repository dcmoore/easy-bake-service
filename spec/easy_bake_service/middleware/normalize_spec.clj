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
      (context "XML"
        (it "doesn't touch the body if the context-type isn't 'application/xml'"
          (let [request {:content-type "something/else"
                         :body (java.io.StringReader. "<some attr=\"data\">thing</some>")}]
            (should=
              (:body request)
              (:body (modified-request request)))))

        (it "converts XML data into hiccup"
          (let [request {:content-type "application/xml"
                         :body (java.io.StringReader. "<some attr=\"data\">thing</some>")}]
            (should=
              [:some {:attr "data"} "thing"]
              (:body (modified-request request)))))

        (it "converts nested XML data into hiccup"
          (let [request {:content-type "application/xml"
                         :body (java.io.StringReader. "<some attr=\"data\"><nested>value</nested><other nested=\"value\" /></some>")}]
            (should=
              [:some {:attr "data"} [:nested {} "value"] [:other {:nested "value"}]]
              (:body (modified-request request)))))

        (xit "normalizes keys to kebab case"
          (let [request {:content-type "application/xml"
                         :body (java.io.StringReader. "<someXml attr_xml=\"data\"></someXml>")}]
            (should=
              [:some-xml {:attr-xml "data"}]
              (:body (modified-request request)))))

        (xit "normalizes nested keys to kebab case"
          (let [request {:content-type "application/xml"
                         :body (java.io.StringReader. "<someXml attr_xml=\"data\"><nestedXml>value</nestedXml></someXml>")}]
            (should=
              [:some-xml {:attr-xml "data"} [:nested-xml {} "value"]]
              (:body (modified-request request))))))

      (context "JSON"
        (it "doesn't touch the body if the content-type isn't 'application/json'"
          (let [request {:content-type "something/else"
                         :body (java.io.StringReader. "{\"top_level_key\":{\"nested_key\":\"value\"}}")}]
            (should=
              (:body request)
              (:body (modified-request request)))))

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
              (:body (modified-request request))))))))

  (context "response"
    (context "converts clojure data to an XML string"
      (it "sets the keys to kebab case"
        (let [response {:status 200
                        :body [:xml-key {:some "attr"} "xml value"]
                        :headers {"Content-Type" "application/xml"}}]
          (should=
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml-key some=\"attr\">xml value</xml-key>"
            (:body (modified-response response)))))

      (it "parses nested xml structures"
        (let [response {:status 200
                        :body [:xml-key {} [:nested-key {} "nested value"]]
                        :headers {"Content-Type" "application/xml"}}]
          (should=
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml-key><nested-key>nested value</nested-key></xml-key>"
            (:body (modified-response response)))))

      (it "parses xml structures with multiple nodes"
        (let [response {:status 200
                        :body [:xml-key {} [:node-1 {} ""] [:node-2 {} ""]]
                        :headers {"Content-Type" "application/xml"}}]
          (should=
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml-key><node-1></node-1><node-2></node-2></xml-key>"
            (:body (modified-response response))))))

    (context "converts clojure data to a JSON string"
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
            (:body (modified-response response)))))

      (xit "should throw an error if json couldn't be parsed"
        (let [response {:status 200
                        :body "not valid json"
                        :headers {"Content-Type" "application/json"}}]
          (should-throw Exception "Invalid JSON in response body"
            (modified-response response)))))

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
