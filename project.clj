(defproject viterbi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :target-path "target/%s"
  :profiles {:test
             {:dependencies [[org.clojure/math.numeric-tower "0.0.4"]]}
             :uberjar {:aot :all}})
