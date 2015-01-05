(defproject new-highs "0.1.0-SNAPSHOT"
  :description "Keep track of shares on the ASX that are making new yearly highs."
  :url "https://github.com/eldritchideen/new-highs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.jsoup/jsoup "1.7.3"]
                 [clj-time "0.9.0-beta1"]
                 [org.clojure/tools.trace "0.7.8"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main ^:skip-aot new-highs.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
