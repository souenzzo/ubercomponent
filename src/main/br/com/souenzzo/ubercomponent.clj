(ns br.com.souenzzo.ubercomponent
  (:require [com.stuartsierra.component :as component]
            [ubergraph.core :as uber]
            [ubergraph.alg :as uber.alg]
            [clojure.spec.alpha :as s]))

(defn system?
  [x]
  (satisfies? component/Lifecycle x))

(defn system->digraph-inits
  [system]
  (for [[from component] system
        :let [edges (component/dependencies component)]
        [label to] edges
        inits [[from {:label (pr-str from)}]
               [to {:label (pr-str to)}]
               [from
                to
                {:label (pr-str label)}]]]
    inits))

(defn system->digraph
  [system]
  (->> (system->digraph-inits system)
       (apply uber/digraph)))

(defn deps
  [system targets]
  (let [g (system->digraph system)]
    (-> (for [target targets
              node (uber.alg/topsort g target)]
          node)
        (set))))

(s/fdef deps
        :args (s/cat :system system?
                     :targets (s/coll-of keyword?))
        :ret (s/coll-of keyword?))

(defn start
  "Like com.stuartsierra.component/start, but start just what is needed by the target list"
  [system targets]
  (->> (deps system targets)
       (component/start-system system)))

(s/fdef start
        :args (s/cat :system system?
                     :targets (s/coll-of keyword?))
        :ret system?)
