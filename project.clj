(defproject new-highs "0.1.0-SNAPSHOT"
  :description "Keep track of shares on the ASX that are making new yearly highs."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.jsoup/jsoup "1.7.3"]]
  :main ^:skip-aot new-highs.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
