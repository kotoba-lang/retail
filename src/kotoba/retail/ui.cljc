(ns kotoba.retail.ui
  "Operator-facing dashboard for a community retail actor.

  Renders an HTML read-only panel from retail records (inventory, receipts,
  reorder queue) using kotoba-lang/html + css. Pure data → markup: no
  network, no DOM. The governor decides sales/voids/reorders; this view only
  observes."
  (:require [html.core :as html]
            [css.core :as css]
            [kotoba.retail :as retail]))

(def ^:private sheet
  {:rules
   {"body"              {:font-family "system-ui,-apple-system,sans-serif"
                        :margin 0 :color "#1a1a1a" :background "#fafafa"}
    "header.bar"        {:display :flex :align-items :center :gap 12
                        :padding "12px 20px" :background "#fff"
                        :border-bottom "1px solid #e5e5e5"}
    "header.bar h1"     {:font-size 18 :margin 0 :font-weight 600}
    "header.bar .badge" {:margin-left :auto :font-size 12 :color "#666"}
    "main"              {:max-width 960 :margin "24px auto" :padding "0 20px"}
    ".card"             {:background "#fff" :border "1px solid #e5e5e5"
                        :border-radius 8 :padding 16 :margin-bottom 16}
    "h2"                {:margin-top 0 :font-size 15}
    "table"             {:width "100%" :border-collapse :collapse :font-size 14}
    "th, td"            {:text-align :left :padding "8px 10px"
                        :border-bottom "1px solid #f0f0f0"}
    "th"                {:font-weight 600 :color "#555" :font-size 12
                        :text-transform :uppercase :letter-spacing "0.04em"}
    "td.amt"            {:font-variant-numeric :tabular-nums :text-align :right}
    ".ok"               {:color "#137a3f"}
    ".warn"             {:color "#b25c00" :background "#fff8e1"
                        :padding "2px 6px" :border-radius 4}
    ".err"              {:color "#b3261e" :background "#fbe9e7"
                        :padding "2px 6px" :border-radius 4}
    ".muted"            {:color "#888"}}})

(defn- stylesheet [] (html/->html (css/style-node sheet)))

(defn- money [n currency] (str (or n 0) " " (or currency "USD")))

(defn- reorder-badge [inv]
  (if (retail/needs-reorder? inv)
    [:span.warn "reorder"]
    [:span.ok "ok"]))

(defn inventory-table [inventories]
  [:table
   [:thead [:tr [:th "SKU"] [:th "Location"] [:th.amt "Qty"] [:th.amt "Reorder at"] [:th "Status"]]]
   [:tbody (for [i inventories]
             [:tr [:td (:inv/sku i)]
                  [:td (:inv/location i)]
                  [:td.amt (:inv/quantity i)]
                  [:td.amt (or (:inv/reorder-at i) "—")]
                  [:td (reorder-badge i)]])]])

(defn receipt-card [r]
  [:section.card
   [:h2 (str "Receipt " (:receipt/id r))]
   [:table
    [:thead [:tr [:th "SKU"] [:th.amt "Price"] [:th.amt "Qty"] [:th.amt "Net"]]]
    [:tbody (for [li (:receipt/items r)]
              [:tr [:td (:li/sku li)]
                   [:td.amt (:li/price li)]
                   [:td.amt (:li/qty li)]
                   [:td.amt (:li/net li)]])]]
   [:p [:strong "Net: "] (money (:receipt/net r) (:receipt/currency r))
    " · Tax: " (money (:receipt/tax r) (:receipt/currency r))
    " · Total: " (money (:receipt/total r) (:receipt/currency r))]])

(defn dashboard
  "Render a full HTML dashboard page for a retail operator."
  [{:keys [inventories receipts] :as ctx}]
  (html/->html
    [:html
     [:head [:meta {:charset "utf-8"}] [:title "cloud-itonami · retail"]
      [:hiccup/raw (stylesheet)]]
     [:body
      [:header.bar
       [:h1 "Community Retail — Operator Console"]
       [:span.badge "read-only · governor-gated"]]
      [:main
       [:section.card [:h2 "Inventory"] (inventory-table inventories)]
       (for [r receipts] (receipt-card r))]]]))
