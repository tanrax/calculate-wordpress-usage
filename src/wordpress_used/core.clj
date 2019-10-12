(ns wordpress-used.core
  (:require
   [clj-http.client :as client]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   ) (:gen-class))

(def headers {"User-Agent"                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:69.0) Gecko/20100101 Firefox/69.0"
              "Accept"                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
              "Accept-Language"           "es,en-US;q=0.7,en;q=0.3"
              "Accept-Encoding"           "gzip, deflate, br"
              "DNT"                       "1"
              "Connection"                "keep-alive"
              "Upgrade-Insecure-Requests" "1"
              "Pragma"                    "no-cache"
              "Cache-Control"             "no-cache"
              "TE"                        "Trailers"})
(def http-config
  {:headers              headers
   :ignore-unknown-host? true
   :connection-timeout   5000
   :throw-exceptions     false})

(defn read-csv-domains
  "Read CSV file with all domains"
  [url]
  (with-open [reader (io/reader (io/resource url))]
    (doall (csv/read-csv reader))))

(defn wordpress?
  "Check if a web page is generated with WordPress"
  [url]
  (try
    (let [response (client/get (str "http://" url "/") http-config)]
      (every? identity [(re-find (re-pattern "meta.*generator.*WordPress") (:body response))]))
    (catch Exception e
      "timeout")))

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
