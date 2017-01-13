(ns webservice-bank.bank
   (:require [clj-time.core :as t]))

(defn date [year month day]
  "Create local date"
  (t/local-date year month day))

(def bank 
  "Bank for test"
   (atom {:name "nubank" :accounts {}}))


; CREATE ACCOUNT  --------------------------------------------------------------------
; (create-account "Mauricio Junior" 1122334455)


(defn prepare-account [name account-number]
  "Create account"
  (ref {:name name :account-number account-number :operations []}))

(defn create-account [name acc-number]
  "Create bank account"
  (let [acc (prepare-account name acc-number)]
     (swap! bank update-in [:accounts] assoc (:account-number @acc) acc)))


; FIRST STEP CREATE OPERATION  -----------------------------------------------------------------
; (create-operation 12345678 "DEPOSIT ATM" 1000 2017 01 10)

(defn prepare-operation [desc amount year month day]
  "Create transaction"
 (ref {:desc desc :amount amount :date (date year month day)}))

(defn create-operation [acc-number desc amount year month day]
  "Create operation"
  (dosync
    (let [acc (get (:accounts @bank) acc-number)]
        (alter acc update-in [:operations] 
               conj (prepare-operation desc amount year month day)))))

; SECOND STEP GET CURRENT BALANCE  -----------------------------------------------------------------
; (get-balance 12345678)

(defn get-balance [acc-number]
   (let [acc (get (:accounts @bank) acc-number)]
      (let [tran (:operations @acc)]
        (reduce + 0 (vec (map #(-> % deref :amount) tran))))))


; THIRD STEP GET BANK STATMENT  -----------------------------------------------------------------
; (get-balance-per-day op)



(def operations-for-statment
  (atom [])) ; Para acessar cada operacao utilizar @joey @

(def op
  [{:desc "DESC1" :amount 100 :date (date 2017 01 10)}
   {:desc "DESC2" :amount 100 :date (date 2017 01 10)}
   {:desc "DESC3" :amount 200 :date (date 2017 01 11)}
   {:desc "DESC4" :amount 200 :date (date 2017 01 11)}
   {:desc "DESC5" :amount 100 :date (date 2017 01 12)}
   {:desc "DESC6" :amount -50 :date (date 2017 01 12)}
   {:desc "DESC7" :amount -100 :date (date 2017 01 13)}
   {:desc "DESC8" :amount -100 :date (date 2017 01 13)}])


(def get-all-operations-from-bank
  (doseq [acc (vals (:accounts @bank))]
    (swap! operations-for-statment conj (acc :operations))))

(defn get-balance-per-day [txns]
  (->> txns (partition-by :date) 
    (map 
      (fn [[{date :date} :as txns]]
        {:date date :balance 
         (reduce + (map :amount txns))}))))

(defn get-balance [acc-number]
   (let [acc (get (:accounts @bank) acc-number)]
      (let [tran (:operations @acc)]
        (reduce + 0 (vec (map #(-> % deref :amount) tran))))))

(def vetor
  [(ref {:desc "DESC1" :amount 100})
   (ref {:desc "DESC2" :amount 200})
   (ref {:desc "DESC3" :amount 300})])




; FORTH STEP PERIODS OF DEBIT  -----------------------------------------------------------------
; (get-all-operations-from-acc 12345678)
; (get-periods-of-debit op2)

(def operations-for-debit
  (atom []))

(def op2
  [{:desc "DESC1" :amount 100 :date (date 2017 01 10)}
   {:desc "DESC2" :amount 100 :date (date 2017 01 10)}
   {:desc "DESC3" :amount 200 :date (date 2017 01 11)}
   {:desc "DESC4" :amount 200 :date (date 2017 01 11)}
   {:desc "DESC5" :amount 100 :date (date 2017 01 12)}
   {:desc "DESC6" :amount -50 :date (date 2017 01 12)}
   {:desc "DESC7" :amount -100 :date (date 2017 01 13)}
   {:desc "DESC8" :amount -100 :date (date 2017 01 13)}])

(defn get-all-operations-from-acc [acc-number]
   (let [acc (get (:accounts @bank) acc-number)]
     (doseq [tran (:operations @acc)]
        (swap! operations-for-debit conj tran :amount))))
;;  (reduce + (map :amount txns))}))))

(defn negative [trx]
  (prn (str (trx :date) ":" (trx :balance))))

(defn get-periods-of-debit [operations]
  (doseq [trx (get-balance-per-day operations)]
    (if (pos? (trx :balance))
      :true
       (negative trx))))


; TEST ----------------------------------------------------------------------------



(def joey 
  "Account for tests"
  (prepare-account "joey" 12345678))

(def paul 
  "Account for tests"
  (prepare-account "paul" 87654321))

(def create-account-test
  "Add account for test into the bank"
  (dosync
    (swap! bank update-in [:accounts] assoc (:account-number @joey) joey)
    (swap! bank update-in [:accounts] assoc (:account-number @paul) paul)))

(def create-operation-test
  "Create operation for test"
  (dosync
    (let [account (get (:accounts @bank) 12345678)]
        (alter account update-in [:operations] 
               conj (prepare-operation "DESC1" 100 2017 01 10))
        (alter account update-in [:operations] 
               conj (prepare-operation "DESC2" 200 2017 01 10))
        (alter account update-in [:operations] 
               conj (prepare-operation "DESC3" -50 2017 01 10)))))


