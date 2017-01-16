(ns webservice-bank.stop-three
  (:require [clj-time.core :as t]))

(def test-bank
  (atom {:name "nubank"
         :accounts {12345678 {:name "My Account"
                              :account-number 12345678
                              :operations [{:amount 100 :date (t/local-date 2017 01 15)}
                                           {:amount -50 :date (t/local-date 2017 01 18)}
                                           {:amount -25 :date (t/local-date 2017 01 16)}]}
                    87654321 {:name "Other Account"
                              :account-number 87654321
                              :operations [{:amount 1000 :date (t/local-date 2017 01 16)}
                                           {:amount 50 :date (t/local-date 2017 01 17)}
                                           {:amount -450 :date (t/local-date 2017 01 18)}]}}}))

(defn all-operations [bank]
  (->> @bank :accounts vals (mapcat :operations)))

(defn ordered-by-date [operations]
  "Ensures operations are sorted by date ascending, regardless of the year that they happen in"
  (sort-by :date t/before? operations))

(defn unique-date [date]
  "Guarantees a unique value for dates across month and year boundaries"
  (+ (.getDayOfYear date)
     (* 1000 (t/year date))))

(defn all-ops-date-partitioned [bank]
  "Gets all operations that have been performed within a bank ordered and partitioned by the
  day that they posted."
  (->> (all-operations bank)
       (ordered-by-date)
       (partition-by #(unique-date (:date %)))))

(defn daily-summary [bank]
  "Gets a summary of operations that occured within a bank by day, including the operations for
  that day and the end of day balance."
  (:days
   (reduce (fn [acc day-ops]
             (let [daily-sum (apply + (map :amount day-ops))
                   eod-balance (+ (:balance acc) daily-sum)
                   date (-> day-ops first :date)
                   day {:date (str (t/year date) "-" (t/month date) "-" (t/day date))
                        :ops day-ops
                        :eod-balance eod-balance}]
               (-> acc
                   (assoc :balance eod-balance)
                   (update-in [:days] conj day))))
           {:balance 0, :days []}
           (all-ops-date-partitioned bank))))

(comment
  (clojure.pprint/pprint
   (daily-summary test-bank)))

