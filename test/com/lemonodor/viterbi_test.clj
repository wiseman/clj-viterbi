(ns com.lemonodor.viterbi-test
  (:require [clojure.test :refer :all]
            [clojure.math.numeric-tower :as math]
            [com.lemonodor.viterbi :as viterbi]))


(def epsilon 0.00001)

(defn nearly= [a b]
  (< (math/abs (- a b)) epsilon))


;; From https://en.wikipedia.org/wiki/Viterbi_algorithm#Example
(def illness-hmm
  (viterbi/make-hmm
   :states [:healthy :fever]
   :start-p {:healthy 0.6, :fever 0.4}
   :trans-p (let [t {:healthy {:healthy 0.7, :fever 0.3},
                     :fever {:healthy 0.4, :fever 0.6}}]
              #((t %1) %2))
   :emit-p (let [t {:healthy {:normal 0.5, :cold 0.4, :dizzy 0.1},
                    :fever {:normal 0.1, :cold 0.3, :dizzy 0.6}}]
             #((t %1) %2))))

;; From http://homepages.ulb.ac.be/~dgonze/TEACHING/viterbi.pdf
(def dna-hmm
  (viterbi/make-hmm
   :states [:H :L]
   :start-p {:H 0.5, :L 0.5}
   :trans-p (let [t {:H {:H 0.5 :L 0.5}
                     :L {:H 0.4 :L 0.6}}]
              #((t %1) %2))
   :emit-p (let [t {:H {\A 0.2 \C 0.3 \G 0.3 \T 0.2}
                    :L {\A 0.3 \C 0.2 \G 0.2 \T 0.3}}]
            #((t %1) %2))))


(deftest hmm-test
  (testing "HMM representation"
    (is (= ((:emit-p illness-hmm) :fever :cold) 0.3))
    (is (= ((:trans-p illness-hmm) :healthy :healthy) 0.7))
    (is (= ((:start-p illness-hmm) :fever) 0.4))))


(deftest viterbi-test
  (testing "Viterbi test"
    (let [[prob path] (viterbi/viterbi illness-hmm [:normal :cold :dizzy])]
      (is (= path [:healthy :healthy :fever]))
      (is (nearly= prob -1.8204482088348124)))
    (let [[prob path] (viterbi/viterbi dna-hmm "GGCACTGAA")]
      (is (= path [:H :H :H :L :L :L :L :L :L]))
      (is (= prob -7.371454956372107)))))

(deftest viterbi-pc-test
  (testing "Viterbi pre-calc test"
    (let [[prob path] (viterbi/viterbi-pc illness-hmm [:normal :cold :dizzy])]
      (is (= path [:healthy :healthy :fever]))
      (is (nearly= prob -1.8204482088348124)))))


;; (deftest array-test
;;   (testing "Java array"
;;     (let [probs (make-array Double/TYPE 1000 1000)]
;;       (time
;;        (dotimes [i 1000]
;;          (let [#^doubles a (aget #^objects probs i)]
;;            (dotimes [j 1000]
;;              (aset #^doubles a j 1.0)))))
;;       (time
;;        (dotimes [i 1000]
;;          (dotimes [j 1000]
;;            (let [#^doubles a (aget #^objects probs i)] (aget a j))))))))



;; (defn randoms-summing-to [n sum]
;;   (let [vals (take n (repeatedly #(rand-int 100)))
;;         vals-sum (* sum (reduce + vals))]
;;     (map #(/ % vals-sum) vals)))

;; (defn cartesian-product [colls]
;;   (if (empty? colls)
;;     '(())
;;     (for [x (first colls)
;;           more (cartesian-product (rest colls))]
;;       (cons x more))))

;; (deftest speed-test
;;     (let [words (map #(str (char (+ 65 (mod % 26))) "WOO" (str %)) (range 2000))
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
;;                :start-p start-probs
;;                :trans-p #((trans-probs %1) %2)
;;                :emit-p (fn [s o]
;;                          (if (= (first s) o)
;;                            0.999
;;                            0.001)))]
;;       (testing "non pre-calc"
;;         (viterbi/viterbi hmm [\A \B \C \D \D \E \F \G \G]))
;;       (testing "pre-calc"
;;         (viterbi/viterbi-pc hmm [\A \B \C \D \D \E \F \G \G]))))
