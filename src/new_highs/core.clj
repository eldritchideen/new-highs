(ns new-highs.core
  (:require [new-highs.scraping :as scrape]
            [clojure.string :as string]
            [clj-time.core :as time]
            [clj-time.format :as f]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:gen-class)
  (:import (java.io FileNotFoundException)))

(defn build-string
  "Returns a string consisting of n times char c."
  [n c]
  (apply str (repeat n c)))

(defn number-of-highs
  "Takes a list of stock codes. Returns a seq of [stock-code count] in order of count"
  [shares]
  (sort-by last (frequencies shares)))

(defn print-weekly-highs
  "Seq of [<share code> <number of highs>]"
  [sorted-share-seq]
  (let [max (last (last sorted-share-seq))]
    (doseq [[share-code num-times] sorted-share-seq]
      (println (str (build-string num-times "*")
                    (build-string (- max num-times) " ")
                    " "
                    share-code)))))

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

(defn -main
  "Get the list of shares making new highs from the web, add it to the existing file and report."
  [& args]
  (let [file-name    "./shares.csv"
        share-data   (group-shares-by-day (read-shares-file file-name))
        todays-highs (scrape/get-shares)
        date-of-data (scrape/get-time)
        date-format  (f/formatter "MMMMMMMMMMMMMM dd, yyyy")
        day-of-week  (time/day-of-week (f/parse date-format date-of-data))
        todays-data  (assoc (dissoc share-data day-of-week) day-of-week todays-highs)
        todays-codes (shares-data-to-list todays-data)]
    (print-weekly-highs (number-of-highs todays-codes))
    (write-shares-file file-name todays-data)))
