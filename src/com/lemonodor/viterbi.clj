(ns com.lemonodor.viterbi
  (:use [clojure.pprint]))


(defn indexed [s]
  (map vector (iterate inc 0) s))


(defn make-hmm [& {:keys [obs states start-p emit-p trans-p]}]
  {:obs obs
   :states states
   :start-p start-p
   :emit-p emit-p
   :trans-p trans-p})


(defn initialize [hmm]
  (let [{:keys [obs states start-p emit-p]} hmm
        path (into {} (for [y states] [y [y]]))
        v (into {} (for [y states]
                     [y
                      (+ (Math/log10 (start-p y))
                         (Math/log10 (emit-p y (obs 0))))]))]
    [path v]))


(defn candidates-for-state [hmm v t y]
  (let [{:keys [obs states trans-p emit-p]} hmm]
    (map (fn [[i y0]]
           [(+ (v y0)
               (Math/log10 (trans-p y0 y))
               (Math/log10 (emit-p y (obs t))))
            y0])
         (indexed states))))


(defn best-candidate [candidates]
  (apply max-key #(% 0) candidates))


(defn run-step [hmm prev-v path t]
  (let [{:keys [obs states trans-p emit-p]} hmm]
    (map (fn [y]
           (let [candidates (candidates-for-state hmm prev-v t y)
                 [prob state] (best-candidate candidates)]
             [[y prob]
              [y (conj (path state) y)]]))
         states)))

(defn argmax [coll]
  (loop [s (indexed coll)
         max (first s)]
    (if (empty? s)
      max
      (let [[idx elt] (first s)
            [max-indx max-elt] max]
        (if (> elt max-elt)
          (recur (rest s) (first s))
          (recur (rest s) max))))))

(defn pprint-hmm [hmm]
  (println "number of states: " (:n hmm) " number of observations:  " (:m hmm))
  (print "init probabilities: ") (pprint (:init-probs hmm))
  (print "emission probs: " ) (pprint (:emission-probs hmm))
  (print "state-transitions: " ) (pprint (:state-transitions hmm)))


(def example-hmm
  (make-hmm
   :states [:healthy :fever]
   :obs [:normal :cold :dizzy]
   :start-p {:healthy 0.6, :fever 0.4}
   :trans-p (let [t {:healthy {:healthy 0.7, :fever 0.3},
                     :fever {:healthy 0.4, :fever 0.6}}]
              #((t %1) %2))
   :emit-p (let [t {:healthy {:normal 0.5, :cold 0.4, :dizzy 0.1},
                    :fever {:normal 0.1, :cold 0.3, :dizzy 0.6}}]
             #((t %1) %2))))
