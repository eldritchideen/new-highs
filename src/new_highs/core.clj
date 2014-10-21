(ns new-highs.core
  (:require [new-highs.scraping :as scrape]
            [new-highs.allords :refer :all]
            [clojure.string :as string]
            [clj-time.core :as time]
            [clj-time.format :as f]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:gen-class)
  (:import (java.io FileNotFoundException Writer)
           (java.util Calendar)))

(defn build-string
  "Returns a string consisting of n times char c."
  [n c]
  (apply str (repeat n c)))

(defn number-of-highs
  "Takes a list of stock codes. Returns a seq of [stock-code count] in order of count"
  [shares]
  (sort-by last (frequencies shares)))

(defn print-weekly-highs
  "Seq of strings"
  [share-strings-seq]
  (doseq [s share-strings-seq]
      (println s)))


(defn weekly-highs-strings
  "Seq of [<share code> <number of highs>]"
  [sorted-share-seq]
  (let [max (last (last sorted-share-seq))
        xao (all-ords)]
    (map (fn [[share-code num-times]]
           (str (build-string num-times "*")
                (build-string (- max num-times) " ")
                " "
                share-code
                (if (xao share-code) " (XAO)" ""))) sorted-share-seq)))

(defn read-shares-file
  "Read a CSV file where each line is <day of week>,<share-code>.
   Returns a list of [<day-of-week> <share-code>]."
  [file-name]
  (try
    (with-open [in-file (io/reader file-name)]
      (doall
        (csv/read-csv in-file)))
    (catch FileNotFoundException e (vector))))

(defn shares-map-to-csv
  [shares-map]
  (letfn [(kv-to-rows [[k v]]
           (map vector (repeat (count v) k) v))]
    (partition 2 (flatten (map kv-to-rows shares-map)))))

(defn write-shares-file
  [file-name data]
  (let [csv-rows (shares-map-to-csv data)]
    (with-open [out-file (io/writer file-name)]
      (csv/write-csv out-file csv-rows))))

(defn write-share-results
  [file-name results-seq]
  (with-open [^Writer out-file (io/writer file-name)]
    (doseq [line results-seq]
      (.write out-file line)
      (.write out-file "\n"))))

(defn group-shares-by-day
  [share-list]
  (reduce
    (fn [ret elem]
      (let [[k v] elem]
        (assoc ret (Long. k) (conj (get ret (Long. k) []) v))))
    {} share-list))

(defn shares-data-to-list
  [shares]
  (flatten
    (reduce
      (fn [ret elem]
        (conj ret (second elem)))
      []
      shares)))

(defn file-name
  [path extension]
  (let [date         (doto (Calendar/getInstance) (.set Calendar/DAY_OF_WEEK 1))
        week-in-year (. date get Calendar/WEEK_OF_YEAR)
        year         (. date get Calendar/YEAR)
        month        (inc (. date get Calendar/MONTH))
        day          (. date get Calendar/DAY_OF_MONTH)]
    (str path year "-" month "-" day "-wk-" week-in-year "." extension )
    ))

(defn -main
  "Get the list of shares making new highs from the web, add it to the existing file and report."
  [& args]
  (let [shares-file  (file-name "./" "csv")
        output-file  (file-name "./" "txt")
        share-data   (group-shares-by-day (read-shares-file shares-file))
        todays-highs (scrape/get-shares)
        date-of-data (scrape/get-time)
        date-format  (f/formatter "MMMMMMMMMMMMMM dd, yyyy")
        day-of-week  (time/day-of-week (f/parse date-format date-of-data))
        todays-data  (assoc (dissoc share-data day-of-week) day-of-week todays-highs)
        todays-codes (shares-data-to-list todays-data)
        output-data  (weekly-highs-strings (number-of-highs todays-codes))]
    (print-weekly-highs output-data)
    (write-shares-file shares-file todays-data)
    (write-share-results output-file output-data)))
