(ns com.lemonodor.viterbi-test
  (:require [clojure.test :refer :all]
            [com.lemonodor.viterbi :as viterbi]))

(def example-hmm
  (viterbi/make-hmm
   :states [:healthy :fever]
   :obs [:normal :cold :dizzy]
   :start-p {:healthy 0.6, :fever 0.4}
   :trans-p (let [t {:healthy {:healthy 0.7, :fever 0.3},
                     :fever {:healthy 0.4, :fever 0.6}}]
              #((t %1) %2))
   :emit-p (let [t {:healthy {:normal 0.5, :cold 0.4, :dizzy 0.1},
                    :fever {:normal 0.1, :cold 0.3, :dizzy 0.6}}]
             #((t %1) %2))))

(deftest hmm-test
  (testing "HMM representation"
    (is (= ((:emit-p example-hmm) :fever :cold) 0.3))
    (is (= ((:trans-p example-hmm) :healthy :healthy) 0.7))
    (is (= ((:start-p example-hmm) :fever) 0.4))))

;; (deftest initialization-test
;;   (testing "table initialization"
;;     (let [[path v] (viterbi/initialize example-hmm)]
;;       (let [candidates (viterbi/candidates-for-state example-hmm v 1 :healthy)]
;;         (println candidates)
;;         (println (viterbi/best-candidate candidates))))))

(deftest step-test
  (testing "stepping algorithm"
    (println (viterbi/viterbi example-hmm))))


(defn randoms-summing-to [n sum]
  (let [vals (map (fn [_] (rand-int 100))
                  (range n))
        vals-sum (* sum (reduce + vals))]
    (map #(/ % vals-sum) vals)))

(defn cartesian-product [colls]
  (if (empty? colls)
    '(())
    (for [x (first colls)
          more (cartesian-product (rest colls))]
      (cons x more))))

;; (deftest speed-test
;;   (testing "speed"
;;     (let [words (map #(str (char (+ 65 (mod % 26))) "WOO") (range 2000))
;;           start-probs (into {}
;;                             (map (fn [w p] [w p])
;;                                  words
;;                                  (randoms-summing-to (count words) 1.0)))
;;           bigrams (cartesian-product words)
;;           trans-probs (into {}
;;                             (map (fn [w1]
;;                                    [w1
;;                                     (into {}
;;                                           (map (fn [w2 p]
;;                                                  [w2 p])
;;                                                words
;;                                                (randoms-summing-to (count words) 1.0)))])
;;                                  words))
;;           hmm (viterbi/make-hmm
;;                :states words
;;                :obs [\A \B \C \D \D \E \F \G]
;;                :start-p start-probs
;;                :trans-p #((trans-probs %1) %2)
;;                :emit-p (fn [s o]
;;                          (if (= (first s) o)
;;                            0.999
;;                            0.001)))]
;;       (println "\n----------------------------------------REALLY DOING IT\n")
;;       (println (viterbi/viterbi hmm)))))
