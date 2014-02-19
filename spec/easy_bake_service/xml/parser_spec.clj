(ns easy-bake-service.xml.parser-spec
  (:require
    [easy-bake-service.xml.parser :refer :all]
    [speclj.core                  :refer :all]))


(describe "XML Parser"
  (it "takes a basic XML string and converts it to Clojure data in Hiccup format"
    (should=
      [:my-tag {} "value"]
      (xml-str->hiccup "<my-tag>value</my-tag>")))

  (it "parses XML attributes"
    (should=
      [:my-tag {:attr "attr value"} "value"]
      (xml-str->hiccup "<my-tag attr=\"attr value\">value</my-tag>")))

  (it "parses nested XML structures"
    (should=
      [:my-tag {} [:key {} [:nested {} "nested value"]]]
      (xml-str->hiccup "<my-tag><key><nested>nested value</nested></key></my-tag>")))

  (it "parses XML with multiple children nodes"
    (should=
      [:my-tag {} [:child-1 {}] [:child-2 {}]]
      (xml-str->hiccup "<my-tag><child-1></child-1><child-2></child-2></my-tag>")))

  (it "has a body with empty strings"
    (should=
      [:my-tag {} "\"sup\""]
      (xml-str->hiccup "<my-tag>\"sup\"</my-tag>"))))
