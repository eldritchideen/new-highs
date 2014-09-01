(ns new-highs.core-test
  (:require [clojure.test :refer :all]
            [new-highs.core :refer :all]))

; test data based on what will be read in from the file.
(def shares [[1 "abc"] [1 "bhp"] [1 "sol"] [2 "rio"] [2 "abc"]])
(def new-share-data [[2 "rio"] [2 "abc"] [2 "xyz"]])

(deftest test-number-of-highs
  (let [test-data ["abc" "rio" "bol" "abc" "bhp" "rio" "arp" "arp" "arp"]]
    (is (= (number-of-highs test-data)
           '(["bol" 1] ["bhp" 1] ["abc" 2] ["rio" 2] ["arp" 3])))
    (is (= (number-of-highs []) []))))

