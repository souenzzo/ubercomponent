# ubercomponent

Bridge between [ubergraph](https://github.com/Engelberg/ubergraph) and [component](https://github.com/stuartsierra/component) 

## Usage

Add to your `deps.edn`
```clojure
br.com.souenzzo/ubercomponent {:git/url "https://github.com/souenzzo/ubercomponent.git"
                               :sha     "3099a2acba6a3423c3b1c53e2cc4e6c882545ed3"}
```

## Example

```clojure
#_(require
    '[br.com.souenzzo.ubercomponent :as ubercomponent]
    '[com.stuartsierra.component :as component]
    '[ubergraph.core :as uber])
(let [system (-> (component/system-map
                   ::rest1 (->Http)
                   ::rest2 (->Http)
                   ::db1 (->Http)
                   ::db2 (->Http)
                   ::cache (->Redis)
                   ::gql (->GraphQL))
                 (component/system-using
                   {::rest1 {:db ::db1}
                    ::rest2 {:db    ::db1
                             :cache ::cache}
                    ::gql   {:db1   ::db1
                             :db2   ::db2
                             :cache ::cache}}))]
  ;; start just the components needed by ::rest1, in the correct order.
  (ubercomponent/start system [::rest1])

  ;; start just the components needed by ::rest2, in the correct order.
  (ubercomponent/start system [::rest2])

  ;; start just the components needed by ::rest1 and ::rest2, in the correct order.
  (ubercomponent/start system [::rest1 ::rest2])

  ;; will start everything
  (component/start system)

  ;; pop-up a window with the dependency graph
  (-> (ubercomponent/system->digraph system)
      (uber/viz-graph))

  ;; show which components are needed to start gql
  (ubercomponent/deps system [::gql])) 
```
