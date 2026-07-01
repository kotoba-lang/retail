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

(deftest ean13-edge-cases
  (testing "13-digit exactly is accepted when checksum valid"
    (is (retail/ean13-valid? "4006381333931")))
  (testing "12 digits is too short"
    (is (not (retail/ean13-valid? "400638133393"))))
  (testing "14 digits is too long"
    (is (not (retail/ean13-valid? "40063813339311"))))
  (testing "non-string is rejected"
    (is (not (retail/ean13-valid? nil)))))

(deftest inventory-edge-cases
  (testing "no reorder point means no reorder"
    (is (not (retail/needs-reorder? (retail/inventory "S1" "L1" 0)))))
  (testing "exactly at reorder point triggers reorder"
    (is (retail/needs-reorder? (retail/inventory "S1" "L1" 5 :reorder-at 5))))
  (testing "empty receipt totals zero"
    (let [r (retail/receipt "R1" [])]
      (is (zero? (:receipt/net r)))
      (is (zero? (:receipt/total r))))))
