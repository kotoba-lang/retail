(ns kotoba.retail-test
  (:require [clojure.test :refer [deftest is testing]]
            [kotoba.retail :as retail]))

;; 4006381333931 is a valid EAN-13 (GS1 test shape, mod-10 correct).
(def ean "4006381333931")

(deftest ean13-test
  (is (retail/ean13-valid? ean))
  (is (not (retail/ean13-valid? "4006381333932"))) ; bad checksum
  (is (not (retail/ean13-valid? "123")))           ; wrong length
  (is (not (retail/ean13-valid? nil))))

(deftest validate-ean13-test
  (is (true? (:retail/valid? (retail/validate-ean13 ean))))
  (is (= :bad-checksum (:retail/error (retail/validate-ean13 "4006381333932")))))

(deftest inventory-test
  (is (retail/needs-reorder? (retail/inventory "S1" "L1" 3 :reorder-at 5)))
  (is (not (retail/needs-reorder? (retail/inventory "S1" "L1" 10 :reorder-at 5)))))

(deftest receipt-test
  (let [li [(retail/line-item "S1" 200 2)               ; net 400
            (retail/line-item "S2" 500 1 :tax-rate 0.1)]  ; net 500 tax 50
        r (retail/receipt "R1" li :currency "USD")]
    (is (= 900 (:receipt/net r)))
    (is (= 50 (:receipt/tax r)))
    (is (= 950 (:receipt/total r)))))
