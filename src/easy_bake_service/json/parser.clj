(ns easy-bake-service.json.parser
  (:require [clojure.data.json :as json]))

(defn json-str->clj-map [json-str]
  (json/read-str json-str))

(defn clj-map->json-str [clj-map]
 (json/write-str clj-map))
