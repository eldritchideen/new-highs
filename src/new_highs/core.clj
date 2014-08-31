(ns new-highs.core
  (:require [new-highs.scraping :as scrape]
            [clojure.string :as string])
  (:gen-class))


; test data based on what will be read in from the file.
(def stocks ["abc" "bhp" "sol" "rio" "abc" "coh" "sol" "abc" "abc" "gpt" "bol" "bol" "sol"])

(defn build-string
  "Returns a string consisting of n times char c."
  [n c]
  (apply str (repeat n c)))

(defn number-of-highs
  "Takes a list of stock codes. Returns a seq of [stock-code count] in order of count"
  [stocks]
  (sort-by last (map (fn [[k v]] [k (count v)])
                     (group-by identity stocks))))

(defn print-weekly-highs
  [sorted-stock-seq]
  (let [max (last (last sorted-stock-seq))]
    (loop [[[stock-code num-times] & more] sorted-stock-seq]
      (if stock-code
        (do
          (println (str (build-string num-times "*")
                        (build-string (- max num-times) " ")
                        " "
                        stock-code))
          (recur more))
        nil))))

(defn -main
  "Get the list of shares making new highs from the web, add it to the existing file and report."
  [& args]
  ; Get current new highs from web and write them out to file.
  (spit "shares.txt" (string/join "\n" (scrape/get-shares)) :append true)
  ; Read in all new highs being tracked to date and draw chart of how many have made
  ; reoccuring new highs since we started tracking.
  (let [all-shares (string/split-lines (slurp "shares.txt"))]
    (print-weekly-highs (number-of-highs all-shares))))
