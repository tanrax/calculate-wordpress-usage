(ns wordpress-used.core
  (:require
   [clj-http.client :as client]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   ) (:gen-class))

(defn read-csv-domains
  "Read CSV file with all domains"
  [url]
  (with-open [reader (io/reader (io/resource url))]
    (doall (csv/read-csv reader))))

(defn wordpress?
  "Check if a web page is generated with WordPress"
  [url]
  (let [response (client/get (str "http://" url "/") {:ignore-unknown-host? true, :connection-timeout 5000, :throw-exceptions false})]
    (every? identity [(re-find (re-pattern "meta.*generator.*WordPress") (:body response))])))

(defn -main
  [& args]
  (let [;; Name of the file containing the CSV with the domains
        file-csv          "top-1m-test.csv"
        ;; Get domains from CSV
        domains-csv       (vec (read-csv-domains file-csv))
        ;; Filters leaving those that have not been checked
        domains-unchecked (vec (filter #(= (get % 2) "nil") domains-csv))]
    ;; List with domains with a boolean indicating if it is generate or not in WordPress
    (doseq [domain-data domains-unchecked] (let [line            (get domain-data 0)
                                                 url             (get domain-data 1)
                                                 ;; Check if domain it is generate or not in WordPress
                                                 check-wordpress (wordpress? url)]
                                             ;; Show info
                                             (prn (str line " " url " " check-wordpress))
                                             ;; Edit domains-csv with check WordPress 
                                             (shell/sh "sed" "-i" (str line "s/nil/" check-wordpress "/g") (str "resources/" file-csv))))))
