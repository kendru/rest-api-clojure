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
  "Get balance from account"
   (let [acc (get (:accounts @bank) acc-number)]
      (let [tran (:operations @acc)]
        (reduce + 0 (vec (map #(-> % deref :amount) tran))))))


; THIRD STEP GET BANK STATMENT  -----------------------------------------------------------------
; (get-balance-per-day op)

(def operations-test (atom [])) 

(defn get-all-operations-from-bank []
  (doseq [acc (vals (:accounts @bank))]
   (doseq [op (acc :operations)]
     (swap! operations-test conj @op))))

(defn teste [] ; Traz todas as operacoes de todas as contas colocar no get-balance-per-day
  (let [acc (:accounts @bank)]
    (map #(-> % deref :operations) (vals acc))))

(defn test2 []
  (let [operations (teste)]
    operations))

(defn get-balance-per-day []
  (get-all-operations-from-bank)
  (->> @operations-test (partition-by :date) 
    (map 
      (fn [[{date :date} :as operations-test]]
        {:date date :balance 
         (reduce + (map :amount operations-test))}))))


; FORTH STEP PERIODS OF DEBIT  -----------------------------------------------------------------
; (get-all-operations-from-acc 12345678)
; (get-periods-of-debit op2)

(def trxs-acc (atom [])) ; a resposta esta vindo com dois vector [[ ]]

(defn get-all-operations-from-account []
   (doseq [op (@joey :operations)]
     (swap! trxs-acc conj @op)))

(defn get-balance-per-day-account []
  (get-all-operations-from-account)
  (->> @trxs-acc (partition-by :date) 
    (map 
      (fn [[{date :date} :as trxs-acc]]
        {:date date :balance 
         (reduce + (map :amount trxs-acc))}))))

(defn negative []
  (when (neg? (get-balance (@joey :account-number)))
   ))

; (filter #(neg? :balance) (get-balance-per-day-account))

; (map )

; (filter #(= (count %) 1) ["a" "aa" "b" "n" "f" "lisp" "clojure" "q" ""])
    
; (create-operation 12345678 "DEPOSIT ATM" -1000 2017 01 10)
; (get-balance 12345678)




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
               conj (prepare-operation "DESC3" -500 2017 01 11)))))


