(ns com.lemonodor.viterbi
  (:require [clojure.pprint :as pprint]
            [clojure.string :as string]))


(defn indexed [s]
  (map vector (iterate inc 0) s))


(defn make-hmm [& {:keys [states start-p emit-p trans-p]}]
  {:states states
   :start-p start-p
   :emit-p emit-p
   :trans-p trans-p})


(defn initialize [hmm obs]
  (let [{:keys [states start-p emit-p]} hmm
        path (into {} (for [y states] [y [y]]))
        v (into {} (for [y states]
                     [y
                      (+ (Math/log10 (start-p y))
                         (Math/log10 (emit-p y (obs 0))))]))]
    [path v]))


(defn candidates-for-state [hmm obs-t v y]
  (let [{:keys [states trans-p emit-p]} hmm]
    (let [candidates
          (map (fn [y0]
                 [(+ (v y0)
                     (Math/log10 (trans-p y0 y))
                     (Math/log10 (emit-p y obs-t)))
                  y0])
               states)]
      candidates)))


(defn best-candidate [candidates]
  (apply max-key #(% 0) candidates))


(defn run-step [hmm obs prev-v path t]
  ;;(println "prev-v:" prev-v)
  (let [{:keys [states trans-p emit-p]} hmm
        obs-t (obs t)
        updates (pmap (fn [y]
                       (let [candidates (candidates-for-state hmm obs-t prev-v y)
                             [prob state] (best-candidate candidates)]
                         ;;(println
                         ;; (str "  candidates for state " y ": " (apply list candidates)))
                         ;;(println "  best candidate:" [prob state])
                         [
                          ;; Map entry V[y] -> prob
                          [y prob]
                          ;; Map entry newpath[y] -> path[state] + [y]
                          [y (conj (path state) y)]]))
                     states)]
    [(into {} (map first updates))
     (into {} (map second updates))]))


(defn print-dptable [v]
  (let [s (str "    "
               (string/join " "
                            (for [i (range (count v))] (format "%12d" i)))
               "\n")]
    (println
     (reduce (fn [s y]
               (str s
                    (format "%-10s" y)
                    (string/join " "
                                 (for [vc v] (format "%.7s" (format "%f" (vc y)))))
                    "\n"))
             s
             (keys (v 0))))))


(defn viterbi [hmm obs]
  (let [[path vc] (initialize hmm obs)
        ;; _ (do
        ;;     (println "----")
        ;;     (println (str "T=" 0))
        ;;     (print-dptable [vc]))
        [v path]
        (loop [path path
               v [vc]
               t 1]
          (if (= t (count obs))
            [v path]
            (do
              ;;(println "----")
              ;;(println (str "T=" t))
              (let [[vc path] (run-step hmm obs (last v) path t)]
                ;;(println "  path" path)
                ;;(print-dptable (conj v vc))
                (recur path (conj v vc) (+ t 1))))))]
        (let [[prob state] (apply max-key #(% 0) (for [y (:states hmm)]
                                                   [((v (- (count obs) 1)) y)
                                                    y]))]
      [(Math/pow 10.0 prob) (path state)])))


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
  (print "init probabilities: ") (pprint/pprint (:init-probs hmm))
  (print "emission probs: " ) (pprint/pprint (:emission-probs hmm))
  (print "state-transitions: " ) (pprint/pprint (:state-transitions hmm)))
