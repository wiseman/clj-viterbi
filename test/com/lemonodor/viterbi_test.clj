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

(deftest initialization-test
  (testing "table initialization"
    (let [[path v] (viterbi/initialize example-hmm)]
      (println path)
      (println v)
      (let [candidates (viterbi/candidates-for-state example-hmm v 1 :healthy)]
        (println candidates)
        (println (viterbi/best-candidate candidates))))))

(deftest step-test
  (testing "stepping algorithm"
    (let [[path v] (viterbi/initialize example-hmm)]
      (let [step (viterbi/run-step example-hmm v path 1)]
        (println "step" step)))))
