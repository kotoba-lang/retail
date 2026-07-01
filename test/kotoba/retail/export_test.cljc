(ns kotoba.retail.export-test
  (:require [clojure.test :refer [deftest is testing]]
            [kotoba.retail :as ret]
            [kotoba.retail.export :as ex]))

(def invs [(ret/inventory "S1" "L1" 3 :reorder-at 5)
           (ret/inventory "S2" "L1" 20 :reorder-at 5)])

(def receipts [(ret/receipt "R1" [(ret/line-item "S1" 200 2)] :currency "USD")])

(deftest csv-export
  (testing "inventory CSV flags reorder"
    (let [csv (ex/inventory->csv invs)]
      (is (re-find #"sku,location,quantity,reorder_at,needs_reorder" csv))
      (is (re-find #"S1,L1,3,5,yes" csv))
      (is (re-find #"S2,L1,20,5,no" csv))))
  (testing "receipts CSV carries totals"
    (let [csv (ex/receipts->csv receipts)]
      (is (re-find #"receipt_id,net,tax,total,currency,items" csv))
      (is (re-find #"R1," csv)))))

(deftest json-export
  (testing "inventory JSON is a boolean-carrying array"
    (let [j (ex/inventory->json invs)]
      (is (re-find #"^\[" j))
      (is (re-find #"\"needs_reorder\":true" j))
      (is (re-find #"\"needs_reorder\":false" j))))
  (testing "receipts JSON shape"
    (let [j (ex/receipts->json receipts)]
      (is (re-find #"\"receipt_id\":\"R1\"" j))
      (is (re-find #"\"items\":1" j)))))
