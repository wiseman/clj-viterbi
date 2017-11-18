(defproject com.lemonodor.viterbi "0.1.0"
  :description "Viterbi decoding for Clojure."
  :url "https://github.com/wiseman/clj-viterbi"
  :license {:name "MIT License"
            :url "https://github.com/wiseman/clj-viterbi/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :target-path "target/%s"
  :global-vars {*warn-on-reflection* true}
  :profiles {:test
             {:dependencies [[org.clojure/math.numeric-tower "0.0.4"]]}
             :uberjar {:aot :all}})
