(ns wordpress-used.core
  (:require
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   ) (:gen-class))

(defn request
  "Make a request by means of curl"
  [url]
  (shell/sh "curl" "-L" "-m" "5" "-H" "User-Agent: Firefox" url))


(defn read-csv-domains
  "Read CSV file with all domains"
  [url]
  (with-open [reader (io/reader (io/resource url))]
    (doall (csv/read-csv reader))))

(defn wordpress?
  "Check if a web page is generated with WordPress"
  [url]
  (let [response (request url)]
    (every? identity [(re-find (re-pattern "meta.*generator.*WordPress") (:out response))])))

(defn -main
  [& args]
  (let [;; Name of the file containing the CSV with the domains
        file-csv          "top-1m.csv"
        ;; Get domains from CSV
        domains-csv       (vec (read-csv-domains file-csv))
        ;; Filters leaving those that have not been checked
        domains-unchecked (vec (filter #(= (get % 2) "nil") domains-csv))]
    ;; List with domains with a boolean indicating if it is generate or not in WordPress
    (prn "Start")
    (doseq [domain-data domains-unchecked] (let [line (get domain-data 0)
                                                 url  (get domain-data 1)]
                                             ;; Show info
                                             (prn (str line " " url))
                                             ;; Edit domains-csv with check WordPress
                                             (shell/sh "sed" "-i" (str line "s/nil/" (wordpress? url) "/g") (str "resources/" file-csv))))
    (prn "Complete")))
