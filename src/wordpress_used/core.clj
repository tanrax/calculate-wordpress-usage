(ns wordpress-used.core
  (:require
   [clj-http.client :as client]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   ) (:gen-class))

(defn read-csv-domains
  "Read CSV file with all domains"
  [url]
  (with-open [reader (io/reader (io/resource url))]
    (doall (csv/read-csv reader))))

(defn save-csv-domains
  "Save the list with the domains in a CSV file"
  [url new-domains]
  (with-open [writer (io/writer url)]
    (csv/write-csv writer new-domains)))

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
        domains-unchecked (filter #(= (get % 2) "nil") domains-csv)]
    (def mod-domains-csv domains-csv)
    ;; List with domains with a boolean indicating if it is generate or not in WordPress
    (doseq [domain-data domains-unchecked] (let [domain          (get domain-data 1)
                                                 ;; Check if domain it is generate or not in WordPress
                                                 check-wordpress (wordpress? domain)]
                                             ;; Edit domains-csv with check WordPress 
                                             (def mod-domains-csv (map #(-> (if (= domain (get % 1)) (assoc % 2 (str check-wordpress)) %)) mod-domains-csv))
                                             ;; Save domains to CSV
                                             (save-csv-domains file-csv mod-domains-csv)))))
