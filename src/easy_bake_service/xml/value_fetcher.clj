(ns easy-bake-service.xml.value-fetcher)

(defn tag-extractor [hiccup-node]
  (first hiccup-node))

(defn attr-extractor [hiccup-node]
  (if (map? (second hiccup-node))
    (second hiccup-node)
    {}))

(defn body-extractor [hiccup-node]
  (if (map? (second hiccup-node))
    (rest (rest hiccup-node))
    (rest hiccup-node)))

(defn- get-body-of-each-node [hiccup-nodes]
  (apply concat (map body-extractor hiccup-nodes)))

(defn- filter-nodes-on-key [hiccup-nodes key-to-filter-on]
  (filter #(= key-to-filter-on (tag-extractor %)) hiccup-nodes))

(defn get-hiccup-nodes [hiccup key-path]
  (loop [hiccup-nodes [hiccup]
         key-path key-path]
    (if (or (empty? hiccup-nodes) (empty? key-path))
      hiccup-nodes
      (let [filtered-nodes (filter-nodes-on-key hiccup-nodes (first key-path))]
        (recur
          (if (empty? (rest key-path))
            filtered-nodes
            (get-body-of-each-node filtered-nodes))
          (rest key-path))))))
