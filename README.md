# Easy Bake Service

__Requires Clojure version of 1.4 or higher.__

The goal of this library is to abstract away the following tasks:

  * __DONE__ - normalize inputs & outputs (xml & json) - middleware
  * _under construction_ - document endpoints - lein task
  * _under construction_ - validate inputs - middleware
  * _under construction_ - generate a client - lein task
  * _under construction_ - generate a skeleton service - lein task

## Normalize Inputs & Outputs

The below code example is an example application that showcases how Easy Bake Service can normalize incoming and outgoing requests/responses. Leaving the app free of xml and json parsing concerns.

```clojure
1 : (ns example-app.core
2 :   (:require [compojure.core :refer [defroutes GET POST]]
3 :             [easy-bake-service.middleware.normalize :refer [wrap-normalize]]
4 :             [ring.adapter.jetty :refer [run-jetty]]))
5 :
6 : (defn- xml-response [body]
7 :   {:status 200
8 :    :headers {"Content-Type" "application/xml"}
9 :    :body body})
10:
11: (defn- json-response [body]
12:   {:status 200
13:    :headers {"Content-Type" "application/json"}
14:    :body body})
15:
16: (defroutes handler
17:   (GET "/xml" [] (xml-response [:sup {:dude "not"} "much bro"]))
18:   (GET "/json" [] (json-response {:sup-dude "not much bro"}))
19:   (POST "/echo" request (str (:body request))))
20:
21: (def app
22:   (-> handler
23:       wrap-normalize))
24:
25: (defn -main []
26:   (run-jetty app {:port 4321}))
```

Assuming your project.clj file has all the dependencies listed it needs you should be able to boot this service.

From a setup standpoint, all we have to do is require the wrap-normalize function and wrap it around our app handler.

With the server up, we should be able to run the following curl commands:

```bash
$: curl localhost:4321/json
=> {:sup-dude "not much bro"}

$: curl localhost:4321/xml
=> <?xml version="1.0" encoding="UTF-8"?><sup dude="not">much bro</sup>

$: curl -iX POST localhost:4321/echo -H "Content-Type: application/json" -d '{"supDude":"not much bro"}'
=> {:sup-dude "not much bro"}

$: curl -iX POST localhost:4321/echo -H "Content-Type: application/xml" -d '<?xml version="1.0" encoding="UTF-8"?><sup-dude>Not much bro</sup-dude>'
=> [:sup-dude {} "Not much bro"]
```

As you can see the wrap-normalize automatically normalizes xml and json in any casing to and from clojure data structures with idiomatic clojure casing. The wrap-normalize function knows which format (xml or json) to convert to and from based off of the specified content-type in the request and response.

## License

Copyright © 2013 Dave Moore

Distributed under the Eclipse Public License.
