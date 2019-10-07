(ns wordpress-used.core
  (:require
   [clj-http.client :as client]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   ) (:gen-class))

(defn wordpress?
  "Check site used WordPress with meta generator"
  [url]
  (let [response (client/get (str "http://" url "/") {:ignore-unknown-host? true, :connection-timeout 5000, :throw-exceptions false})]
    (every? identity [(re-find (re-pattern "meta.*generator.*WordPress") (:body response))])))


(defn -main
  [& args]
  ;; Read CSV with all domains
  (with-open [reader (io/reader (clojure.java.io/resource "top-1m-test.csv"))]
    (doall
      (let [domains                (csv/read-csv reader)
            ;; Check is WordPress
            domains-with-wordpress (doall (map #(conj % (wordpress? (get % 1))) domains))]
        ;;domains-with-wordpress (map #(conj % (wordpress? (get % 1))) domains)]
      ;; Save CSV
      (with-open [writer (io/writer (clojure.java.io/resource "top-1m-test.csv"))]
        (csv/write-csv writer (vec domains-with-wordpress)))
      ))))
