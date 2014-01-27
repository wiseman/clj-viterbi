(ns com.lemonodor.viterbi
  (:require [clojure.pprint :as pprint]
            [clojure.set :as set]
            [clojure.string :as string]))


(defn indexed [s]
  (map vector (iterate inc 0) s))


(defn make-hmm [& {:keys [states start-p emit-p trans-p]}]
  {:states states
   :start-p start-p
   :emit-p emit-p
   :trans-p trans-p})


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
  (let [{:keys [states trans-p emit-p]} hmm
        obs-t (obs t)
        updates (pmap (fn [y]
                        (let [candidates (candidates-for-state hmm obs-t prev-v y)
                              [prob state] (best-candidate candidates)]
                          [
                           ;; Map entry V[y] -> prob
                           [y prob]
                           ;; Map entry newpath[y] -> path[state] + [y]
                           [y (conj (path state) y)]]))
                      states)]
    [(into {} (map first updates))
     (into {} (map second updates))]))


(defn viterbi [hmm obs]
  (let [obs (vec obs)
        [path vc] (initialize hmm obs)
        [v path] (loop [path path
                        v [vc]
                        t 1]
                   (if (= t (count obs))
                     [v path]
                     (let [[vc path] (run-step hmm obs (last v) path t)]
                       (recur path (conj v vc) (+ t 1)))))]
    (let [[prob state] (apply max-key #(% 0) (for [y (:states hmm)]
                                               [((v (- (count obs) 1)) y)
                                                y]))]
      [prob (path state)])))


(defn array? [x] (-> x class .isArray))
(defn see [x] (if (array? x) (map see x) x))

(defn initialize-pc [hmm obs]
  (let [{:keys [num-states start-p emit-p]} hmm
        path (into {} (for [y (range num-states)] [y [y]]))
        v (into {} (for [y (range num-states)]
                     [y
                      (+ (aget ^doubles start-p y)
                         (aget ^doubles (aget ^objects emit-p y) (first obs)))]))]
    [path v]))


(defn candidates-for-state-pc [hmm obs-t v y]
  (let [{:keys [num-states trans-p emit-p]} hmm]
    (let [candidates
          (map (fn [y0]
                 [(+ (v y0)
                     (aget ^doubles (aget ^objects trans-p y0) y)
                     (aget ^doubles (aget ^objects emit-p y) obs-t))
                  y0])
               (range num-states))]
      candidates)))

(defn run-step-pc [hmm obs prev-v path t]
  (let [{:keys [num-states]} hmm
        obs-t (nth obs t)
        updates (pmap (fn [y]
                        (let [candidates (candidates-for-state-pc hmm obs-t prev-v y)
                              [prob state] (best-candidate candidates)]
                          [
                           ;; Map entry V[y] -> prob
                           [y prob]
                           ;; Map entry newpath[y] -> path[state] + [y]
                           [y (conj (path state) y)]]))
                      (range num-states))]
    [(into {} (map first updates))
     (into {} (map second updates))]))


(defn viterbi-pc [hmm obs]
  (let [{:keys [states start-p trans-p emit-p]} hmm
        obs (vec obs)
        ;; State -> index
        state-index-map (into {} (map (fn [s i] [s i])
                                      states
                                      (range)))
        index-state-map (set/map-invert state-index-map)
        ;; Observation -> index
        obs-index-map (into {} (map (fn [o i] [o i])
                                    (distinct obs)
                                    (range)))
        index-obs-map (set/map-invert obs-index-map)
        num-states (count states)
        num-distinct-obs (count obs-index-map)
        hmm-pc {:num-states num-states
                :num-distinct-obs num-distinct-obs
                :start-p
                (let [probs (make-array Double/TYPE num-states)]
                  (doseq [[state i] state-index-map]
                    (aset ^doubles probs i ^double (Math/log10 (start-p state))))
                  probs)
                :emit-p
                (let [probs (make-array Double/TYPE num-states num-distinct-obs)]
                  (doseq [[state i] state-index-map]
                    (let [#^doubles row (aget #^objects probs i)]
                      (doseq [[obs j] obs-index-map]
                        (aset ^doubles row j
                              ^double (Math/log10 (emit-p state obs))))))
                  probs)
                :trans-p
                (let [probs (make-array Double/TYPE num-states num-states)]
                  (doseq [[state-i i] state-index-map]
                    (let [#^doubles row (aget #^objects probs i)]
                      (doseq [[state-j j] state-index-map]
                        (aset ^doubles row j
                              ^double (Math/log10 (trans-p state-i state-j))))))
                  probs)}
        obs-i (map obs-index-map obs)
        [path vc] (initialize-pc hmm-pc obs-i)
        [v path] (loop [path path
                        v [vc]
                        t 1]
                   (if (= t (count obs))
                     [v path]
                     (let [[vc path] (run-step-pc hmm-pc obs-i (last v) path t)]
                       (recur path (conj v vc) (+ t 1)))))]
    (let [[prob state] (apply max-key #(% 0) (for [y (range num-states)]
                                               [((v (- (count obs) 1)) y)
                                                y]))]
      [prob (map index-state-map (path state))])))


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
