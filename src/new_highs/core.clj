(ns new-highs.core
  (:require [new-highs.scraping :as scrape]
            [new-highs.allords :refer :all]
            [clj-time.core :as time]
            [clj-time.format :as f]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class)
  (:import (java.io FileNotFoundException PushbackReader)
           (java.util Calendar)))

(def shares-data (ref {}))

(defn- serialise
  "Writes out the state of the share data to a file"
  [file-name]
  (with-open [w (clojure.java.io/writer file-name)]
    (binding [*out* w]
      (pr @shares-data))))

(defn- deserialise
  "Loads share data state. If file doesn't exist return empty hash map."
  [file-name]
  (try
    (with-open [r (PushbackReader. (clojure.java.io/reader file-name))]
      (binding [*read-eval* false]
        (let [data (read r)]
          (dosync
            (ref-set shares-data data)))))
    (catch FileNotFoundException e {})))

(defn- build-string
  "Returns a string consisting of n times char c."
  [n c]
  (apply str (repeat n c)))

(defn- number-of-highs
  "Takes a list of stock codes. Returns a seq of [stock-code count] in order of count"
  [shares]
  (sort-by last (frequencies shares)))

(defn- print-weekly-highs
  "Seq of strings"
  [share-strings-seq]
  (doseq [s share-strings-seq]
    (println s)))


(defn- weekly-highs-strings
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

(defn- shares-data-to-list
  [shares]
  (flatten
    (reduce
      (fn [ret elem]
        (conj ret (second elem)))
      []
      shares)))

(defn- current-dates
  []
  (let [date (doto (Calendar/getInstance) (.set Calendar/DAY_OF_WEEK 1))
        week-in-year (. date get Calendar/WEEK_OF_YEAR)
        year (. date get Calendar/YEAR)
        month (inc (. date get Calendar/MONTH))
        day (. date get Calendar/DAY_OF_MONTH)]
    [(format "%d-%02d-%02d - Wk No. %02d" year month day week-in-year) week-in-year]
    ))

(def cli-options
  [["-d" "--output FILE" "path and file name to store data"
    :default "./share-data.clj"]
   ["-h" "--help"]])

(defn init
  "Initialise the module. Currently just loads the data from file."
  [share-data-filename]
  (deserialise share-data-filename))

(defn shutdown
  "Writes out current share data state to file"
  [share-data-filename]
  (serialise share-data-filename))

(defn update-shares
  "Scrapes the website and updates the data of which shares are making new highs"
  []
  (let [[current-week week-in-year] (current-dates)
        todays-highs                (scrape/get-shares)
        date-of-data (scrape/get-time)
        date-format (f/formatter "MMMMMMMMMMMMMM dd, yyyy")
        day-of-week (time/day-of-week (f/parse date-format date-of-data))]
    (dosync (alter shares-data assoc-in [week-in-year :data day-of-week] todays-highs)
            (alter shares-data assoc-in [week-in-year :date ] current-week))
    (->> (get-in @shares-data [week-in-year :data])
         (shares-data-to-list)
         (number-of-highs)
         (weekly-highs-strings)
         (print-weekly-highs))))

(defn -main
  "Get the list of shares making new highs from the web, add it to the existing file and report."
  [& args]
  (let [options             (parse-opts args cli-options)
        share-data-filename (get-in options [:options :output])]
    (init share-data-filename)
    (update-shares)
    (shutdown share-data-filename)))
