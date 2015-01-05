(ns new-highs.core
  (:require [new-highs.scraping :as scrape]
            [new-highs.allords :refer :all]
            [clj-time.core :as time]
            [clj-time.format :as f]
            [clojure.java.io :as io])
  (:gen-class)
  (:import (java.io FileNotFoundException PushbackReader)
           (java.util Calendar)))

(def share-data-file "./share-data.clj")
(def shares-data (atom {}))

(defn serialise
  "Writes out the state of the share data to a file"
  [file-name]
  (with-open [w (clojure.java.io/writer file-name)]
    (binding [*out* w]
      (pr @shares-data))))

(defn deserialise
  "Loads share data state. If file doesn't exist return empty hash map."
  [file-name]
  (try
    (with-open [r (PushbackReader. (clojure.java.io/reader file-name))]
      (binding [*read-eval* false]
        (let [data (read r)]
          (reset! shares-data data))))
    (catch FileNotFoundException e {})))

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

(defn shares-data-to-list
  [shares]
  (flatten
    (reduce
      (fn [ret elem]
        (conj ret (second elem)))
      []
      shares)))

(defn current-week
  []
  (let [date         (doto (Calendar/getInstance) (.set Calendar/DAY_OF_WEEK 1))
        week-in-year (. date get Calendar/WEEK_OF_YEAR)
        year         (. date get Calendar/YEAR)
        month        (inc (. date get Calendar/MONTH))
        day          (. date get Calendar/DAY_OF_MONTH)]
    (format "%d-%02d-%02d, wk-%02d" year month day week-in-year)
    ))

(defn -main
  "Get the list of shares making new highs from the web, add it to the existing file and report."
  [& args]
  (deserialise share-data-file)
  (let [current-week (current-week)
        todays-highs (scrape/get-shares)
        date-of-data (scrape/get-time)
        date-format  (f/formatter "MMMMMMMMMMMMMM dd, yyyy")
        day-of-week  (time/day-of-week (f/parse date-format date-of-data))]
    (swap! shares-data assoc-in [current-week day-of-week] todays-highs)
    (->> (@shares-data current-week)
         (shares-data-to-list)
         (number-of-highs)
         (weekly-highs-strings)
         (print-weekly-highs))
    (serialise share-data-file)))
