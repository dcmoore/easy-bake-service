(defproject easy-bake-service "0.0.8"
  :description "A simple framework that makes it easy to serve data"
  :url "https://github.com/dcmoore/easy-bake-service"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [speclj "2.5.0"]
                 [ring/ring-codec "1.0.0"]
                 [camel-snake-kebab "0.3.2"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.clojure/data.json "0.2.1"]]
  :profiles {:dev {:dependencies [[speclj "2.5.0"]]}}
  :test-paths ["spec/"]
  :plugins [[speclj "2.5.0"]])
