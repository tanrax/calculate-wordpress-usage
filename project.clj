(defproject wordpress-used "1.0.0-SNAPSHOT"
  :description "Calculates WordPress usage index from a CSV list of domains"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.csv "0.1.4"]]
  :jvm-opts ["-Xmx1G"]
  :main ^:skip-aot wordpress-used.core
  :repl-options {:init-ns wordpress-used.core})
