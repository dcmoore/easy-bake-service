(ns easy-bake-service.middleware.normalize-json-spec
  (:use
    [speclj.core]
    [easy-bake-service.middleware.normalize-json]))


(defn modified-request [test-request]
  (let [arbitrary-handler (fn [request] request)]
    ((wrap-json arbitrary-handler) test-request)))

(defn request-body [test-request]
  (:body (modified-request test-request)))


(describe "wrap-json"
  (with test-request {:headers {"content-type" "application/json"}
                      :body    "{\"someKey\":\"some value\"}"})

  (context "altering the request"
    (it "won't alter the request unless the content-type is set to application/json"
      (let [request {:body "{\"someKey\":\"some value\"}" :headers {}}]
        (should= request (modified-request request))))

    (it "turns camel cased json into dasherized clojure data"
      (should=
        {:some-key "some value"}
        (request-body @test-request)))

    (it "really turns camel cased json into dasherized clojure data"
      (should=
        {:fa-real "dawg"}
        (request-body (assoc @test-request :body "{\"faReal\":\"dawg\"}"))))

    (it "returns a 400 bad request if there are spaces in the json string keys"
      (should=
        400
        (:status (modified-request (assoc @test-request :body "{\"fa real\":\"dawg\"}")))))

    (it "handles nested json structures"
      (should=
        {:this {:is {:some "json"}}}
        (request-body (assoc @test-request :body "{\"this\":{\"is\":{\"some\":\"json\"}}}"))))))




(run-specs)
