# kotoba-retail

[![CI](https://github.com/kotoba-lang/retail/actions/workflows/ci.yml/badge.svg)](https://github.com/kotoba-lang/retail/actions/workflows/ci.yml)

**Retail SKUs, EAN-13 barcodes, POS receipts and inventory in pure Clojure.**
A [kotoba-lang](https://github.com/kotoba-lang) capability library for the
[`cloud-itonami-4711`](https://github.com/gftdcojp/cloud-itonami-4711)
community-retail open business: SKU records, EAN-13 (GS1) article numbers
with mod-10 checksum validation, POS line items and receipts, and inventory
stock records with reorder thresholds.

No network, no I/O. Amounts are plain numbers in the smallest currency unit.
Portable `.cljc` across JVM / ClojureScript / SCI / GraalVM.

## Contract

```clojure
(require '[kotoba.retail :as retail])

(retail/ean13-valid? "4006381333931")          ; => true
(retail/sku "S1" :name "Widget" :price 200)
(retail/needs-reorder? (retail/inventory "S1" "L1" 3 :reorder-at 5)) ; => true
(retail/receipt "R1" [(retail/line-item "S1" 200 2)])
```

## License

Apache License 2.0.
