(ns kotoba.retail.export
  "Operator-facing export for a community retail actor.

  Renders retail records to CSV and JSON for end-of-day reconciliation, audit
  export and downstream reporting. Pure data → text: no network, no I/O."
  (:require [clojure.string :as str]
            [kotoba.retail :as retail]))

(defn- csv-cell [v]
  (let [s (str (if (nil? v) "" v))]
    (if (re-find #"[\",\n]" s)
      (str "\"" (str/replace s "\"" "\"\"") "\"")
      s)))

(defn- csv-row [vals] (str/join "," (map csv-cell vals)))

(defn- json-str [v]
  (-> (str (if (nil? v) "" v))
      (str/replace "\\" "\\\\")
      (str/replace "\"" "\\\"")
      (str/replace "\n" "\\n")))

(defn inventory->csv [inventories]
  (str/join "\n"
    (cons (csv-row ["sku" "location" "quantity" "reorder_at" "needs_reorder"])
          (for [i inventories]
            (csv-row [(:inv/sku i)
                      (:inv/location i)
                      (:inv/quantity i)
                      (or (:inv/reorder-at i) "")
                      (if (retail/needs-reorder? i) "yes" "no")])))))

(defn receipts->csv [receipts]
  (str/join "\n"
    (cons (csv-row ["receipt_id" "net" "tax" "total" "currency" "items"])
          (for [r receipts]
            (csv-row [(:receipt/id r)
                      (:receipt/net r)
                      (or (:receipt/tax r) 0)
                      (:receipt/total r)
                      (:receipt/currency r)
                      (count (:receipt/items r))])))))

(defn inventory->json [inventories]
  (str "["
       (str/join ","
                 (for [i inventories]
                   (str "{\"sku\":\"" (json-str (:inv/sku i)) "\","
                        "\"location\":\"" (json-str (:inv/location i)) "\","
                        "\"quantity\":" (or (:inv/quantity i) 0) ","
                        "\"reorder_at\":" (or (:inv/reorder-at i) "null") ","
                        "\"needs_reorder\":" (if (retail/needs-reorder? i) "true" "false") "}")))
       "]"))

(defn receipts->json [receipts]
  (str "["
       (str/join ","
                 (for [r receipts]
                   (str "{\"receipt_id\":\"" (json-str (:receipt/id r)) "\","
                        "\"net\":" (or (:receipt/net r) 0) ","
                        "\"tax\":" (or (:receipt/tax r) 0) ","
                        "\"total\":" (or (:receipt/total r) 0) ","
                        "\"currency\":\"" (or (:receipt/currency r) "USD") "\","
                        "\"items\":" (count (:receipt/items r)) "}")))
       "]"))
