(ns webservice-bank.bank
   (:require [clj-time.core :as t]))

(defn soma [numero]
  (+ 2 numero))

;; You can remove the "date" function, since it does not add any functionality
;; on top of clj-time.core/local-date

;; defonce will prevent this from getting re-evaluated if you re-evaluate this namespace
;; It may make development a bit easier if you use a middleware that reloads your code
;; on change.
(defonce bank
  "Bank for test"
   (atom {:name "nubank" :accounts {}}))


; CREATE ACCOUNT  --------------------------------------------------------------------
; (create-account "Mauricio Junior" 1122334455)

;; I understand the choice of a ref here, but since you are keeping all accounts in an
;; atom, you can guarantee consistency during a swap! operation within an atom. I'll show
;; an alternate implementation of create-operation that assumes that accounts are plain maps
;; rather than refs
(defn prepare-account [name account-number]
  "Create account"
  (ref {:name name
        :account-number account-number
        :operations []}))

(defn create-account [name acc-number]
  "Create bank account"
  (let [acc (prepare-account name acc-number)]
    ;; No need to deref acc when you already have the account number 
    (swap! bank update-in [:accounts] assoc acc-number acc)))


; FIRST STEP CREATE OPERATION  -----------------------------------------------------------------
; (create-operation 12345678 "DEPOSIT ATM" 1000 2017 01 10)

;; You do not need to make an operation a ref because it is immutable.
(defn prepare-operation [desc amount year month day]
  "Create transaction"
  (ref {:desc desc
        :amount amount
        :date (t/local-date year month day)}))

(defn create-operation [acc-number desc amount year month day]
  "Create operation"
  (dosync
    (let [acc (get (:accounts @bank) acc-number)]
        (alter acc update-in [:operations] 
               conj (prepare-operation desc amount year month day)))))

;; Here is an alternate implementation of prepare-operation and create-operation that assumes that
;; only the bank is an atom and accounts and operations are plain data structures. In this case, I'm
;; also passing the bank in as a parameter - it would make testing easier if you re-wrote your create-*
;; functions to accept the bank as a first parameter
(comment
  
  (def example-bank
    (atom {:name "nubank"
           :accounts {12345678 {:name "My Account"
                                :account-number 12345678
                                :operations []}}}))

  (defn prepare-operation [desc amount year month day]
    {:desc desc
     :amount amount
     :date (t/local-date year month day)})

  (defn create-operation [bank acc-number operation]
    {:pre [(get-in @bank [:accounts acc-number])]} ;; Validates that this is a valid account
    (swap! bank update-in [:accounts acc-number operations] conj operation)
    bank)

  ;; Usage:
  (-> bank
      (create-operation 1234567 (prepare-operation "DEPOSIT ATM" 1000 2017 01 10))
      (create-operation 1234567 (prepare-operation "CASH WITHDRAWAL" 500 2017 01 11)))
)

;; A more functional approach may benefit you here: instead of create-operation performing the
;; swap! itself, you could write it as a function that takes a bank data structure and returns
;; a new bank with the appropriate changes made. You could then guarantee consistency across a
;; multi-account transaction by composing operations and performing them in a single swap!
(comment
  (def example-bank
    (atom {:name "nubank"
           :accounts {12345678 {:name "My Account"
                                :account-number 12345678
                                :operations []}
                      87654321 {:name "Other Account"
                                :account-number 87654321
                                :operations []}}}))

  (defn operation [acc-number desc amount year month day]
    (fn [bank]
      (let [operation {:desc desc
                       :amount amount
                       :date (t/local-date year month day)}]
        (update-in bank [:accounts acc-number :operations] conj operation))))

  (defn transact! [bank operations]
    (swap! bank
           ;; This reduce applies each operation in the transaction in order then
           ;; "commits" the final value with swap!
           (fn [bank]
             (reduce
              (fn [curr-bank operation]
                (operation curr-bank))
              bank
              operations))))

  ;; Usage:
  (transact! example-bank [(operation 12345678 "DEPOSIT ATM" 1000 2017 01 10)])
  (transact! example-bank
             [(operation 12345678 "Transfer to 87654321" -250 2017 01 10)
              (operation 87654321 "Transfer from 12345678" 250 2017 01 10)])
)

; SECOND STEP GET CURRENT BALANCE  -----------------------------------------------------------------
; (get-balance 12345678)

(defn get-balance [acc-number]
  "Get balance from account"
   (let [acc (get-in @bank [:accounts acc-num]) ;; Use get-in when going more than 1 level deep
         tran (:operations @acc)] ;; No need to nest a let inside a let 
     (reduce + 0 (map #(-> % deref :amount) tran)))  ;; No need to convert seq to vector)

;; Most of these functions could be simplified if the bank were a single atom. For example:
(comment
  (defn get-balance [bank acc-number]
    (let [ops (get-in @bank [:accounts acc-number :operations])]
      (reduce + 0 (map :amount ops))))
)


; THIRD STEP GET BANK STATMENT  -----------------------------------------------------------------
; (get-balance-per-day op)

;; I could not tell whether the third step is supposed to get a statement for an individual
;; account or for the entire pank

(def operations-test (atom [])) 

;; This function only exists for side effects - you will want to get the operations without
;; first having to put them in another data structure. Something like your "teste" function
;; with mapcat instead of map would give you a vector of all operations that have occurred
;; in the bank.
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
  ;; partition-by is probably not what you want if the transactions are out of order
  (->> @operations-test (partition-by :date)
    (map 
      (fn [[{date :date} :as operations-test]]
        {:date date :balance 
         (reduce + (map :amount operations-test))}))))


; FORTH STEP PERIODS OF DEBIT  -----------------------------------------------------------------
; (get-all-operations-from-acc 12345678)
; (get-periods-of-debit op2)



(def trxs-acc (atom [])) ; a resposta esta vindo com dois vector [[ ]]

(defn get-all-operations-from-account [acc]
   (doseq [op (@acc :operations)]
     (swap! trxs-acc conj @op)))

(defn get-balance-per-day-account []
  (get-all-operations-from-account)
  (->> @trxs-acc (partition-by :date) 
    (map 
      (fn [[{date :date} :as trxs-acc]]
        {:date date :balance 
         (reduce + (map :amount trxs-acc))}))))

;(defn negative []
 ; (when (neg? (get-balance (@joey :account-number)))
  ; ))

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


