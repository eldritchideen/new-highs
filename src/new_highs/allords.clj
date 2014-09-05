(ns new-highs.allords
  (:require [clojure.java.io :as io]
            [clojure.string :as s]))

(defn all-ords
  []
  (let [all-ords-file (s/split-lines (slurp
                                       (io/file
                                         (io/resource "all-ords.txt"))))]
    (reduce (fn [map key] (conj map (hash-map key nil))) {} all-ords-file)))