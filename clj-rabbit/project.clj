(defproject clj-rabbit "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [com.novemberain/langohr "3.5.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 ]
  :main ^:skip-aot clj-rabbit.core
  :target-path "target"
  :profiles {:uberjar {:aot :all}})
