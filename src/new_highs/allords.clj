(ns new-highs.allords
  (:require [clojure.java.io :as io]
            [clojure.string :as s]))

(defn all-ords
  []
  (let [all-ords-file (s/split-lines (slurp (io/resource "all-ords.txt")))]
    (reduce (fn [set e] (conj set e)) #{} all-ords-file)))