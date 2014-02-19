(ns easy-bake-service.xml.value-fetcher-spec
  (:require
    [easy-bake-service.xml.value-fetcher :refer :all]
    [speclj.core                         :refer :all]))

(def hiccup [:key {}
              [:nesting {}
                [:match "first match"]]
              [:nesting
                [:match {} "second match"]]
              [:nesting {}]
              [:miss ""]])

(describe "get-hiccup-nodes"
  (it "gets all of the matching nodes"
    (should==
      [[:match "first match"] [:match {} "second match"]]
      (get-hiccup-nodes hiccup [:key :nesting :match])))

  (it "returns an empty collection if there are no matching nodes"
    (should=
      []
      (get-hiccup-nodes hiccup [:key :wrong-turn :match]))))
