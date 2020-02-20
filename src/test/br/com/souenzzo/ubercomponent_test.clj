(ns br.com.souenzzo.ubercomponent-test
  (:require [clojure.test :refer [deftest testing is]]
            [br.com.souenzzo.ubercomponent :as ubercomponent]
            [clojure.spec.test.alpha :as stest]
            [com.stuartsierra.component :as component]))

(defn my-comp
  [{::keys [id state]
    :as    kv}]
  (with-meta kv
             `{component/start ~(fn [this]
                                  (swap! state conj [:start id])
                                  this)
               component/stop  ~(fn [this]
                                  (swap! state conj [:stop id])
                                  this)}))

(defn sample-system
  [state]
  (component/system-map
    ::admin-ui (my-comp {::id ::admin-ui ::state state})
    ::admin-ui-routes (my-comp {::id ::admin-ui-routes ::state state})
    ::admin-ui-service (my-comp {::id ::admin-ui-service ::state state})
    ::app-entity-conn (my-comp {::id ::app-entity-conn ::state state})
    ::app-hodur (my-comp {::id ::app-hodur ::state state})
    ::app-methods-protocolos (my-comp {::id ::app-methods-protocolos ::state state})
    ::app-parser (my-comp {::id ::app-parser ::state state})
    ::app-register (my-comp {::id ::app-register ::state state})
    ::app-rules (my-comp {::id ::app-rules ::state state})
    ::app-schema (my-comp {::id ::app-schema ::state state})
    ::app-workflows (my-comp {::id ::app-workflows ::state state})
    ::client-service (my-comp {::id ::client-service ::state state})
    ::public-rest (my-comp {::id ::public-rest ::state state})
    ::public-routes (my-comp {::id ::public-routes ::state state})
    ::public-service (my-comp {::id ::public-service ::state state})
    ::schema-info (my-comp {::id ::schema-info ::state state})))

(deftest components
  (stest/instrument)
  (let [state (atom [])
        system (-> (sample-system state)
                   (component/system-using
                     {::app-schema       {:connection         ::app-entity-conn
                                          :meta-db            ::app-hodur
                                          :workflows          ::app-workflows
                                          :methods-protocolos ::app-methods-protocolos}
                      ::schema-info      {:schema-db ::app-schema}
                      ::app-register     {:schema-db ::app-schema}
                      ::app-parser       {:register ::app-register}
                      ::admin-ui-routes  {:parser     ::app-parser
                                          :meta-db    ::app-hodur
                                          :rules      ::app-rules
                                          :connection ::app-entity-conn}
                      ::admin-ui-service {:routes ::admin-ui-routes}
                      ::admin-ui         {:service ::admin-ui-service}
                      ::public-routes    {:parser     ::app-parser
                                          :meta-db    ::app-hodur
                                          :rules      ::app-rules
                                          :connection ::app-entity-conn}
                      ::public-service   {:routes ::public-routes}
                      ::public-rest      {:service ::public-service}}))]
    (testing
      "Should return all deps needed to start"
      (is (= #{::app-entity-conn
               ::app-hodur
               ::app-methods-protocolos
               ::app-parser
               ::app-register
               ::app-rules
               ::app-schema
               ::app-workflows
               ::public-rest
               ::public-routes
               ::public-service}
             (ubercomponent/deps system [::public-rest]))))
    (testing
      "My system should be started in correct order"
      (ubercomponent/start system [::public-rest])
      (is (= @state
             [[:start ::app-hodur]
              [:start ::app-entity-conn]
              [:start ::app-methods-protocolos]
              [:start ::app-workflows]
              [:start ::app-schema]
              [:start ::app-rules]
              [:start ::app-register]
              [:start ::app-parser]
              [:start ::public-routes]
              [:start ::public-service]
              [:start ::public-rest]])))))
