(ns new-highs.scraping
  (:import (org.jsoup Jsoup)
           (org.jsoup.select Elements)
           (org.jsoup.nodes Element)))

(def URL "http://www.smh.com.au/business/markets/52-week-highs?page=-1")

(defn get-page
  [url]
  (.get (Jsoup/connect url)))

(defn get-elems
  [page css]
  (.select page css))

(defn get-shares
  "Fetch the list of shares that have made new highs"
  []
  (let [html (get-page URL)
        elems (get-elems html "#content > section > table > tbody > tr > th > a")]
    (into [] (for [e elems] (.text e)))))