(ns webservice-bank.bank-handler
   (:require
   [clojure.data.json :as json]
   [clojure.walk :as walk]
   [webservice-bank.route :as route]
   [webservice-bank.bank :as bank]))

; JSON ------------------------------------------------------

(defn json-response [data]
  "Convert response to JSON "
  (when data
    {:body (json/write-str data)
     :headers {"Content-type" "application/json"}}))

(defn json-body [request]
  "Convert body to JSON"
 (walk/keywordize-keys 
  (json/read-str (slurp (:body request)))))

(defn json-error-handler [handler]
  "Convert error to JSON"
  (fn [request]
    (try
      (handler request)
      (catch Throwable throwable
        (assoc (json-response {:message (.getMessage throwable)
                               :stacktrace (map str (.getStackStrace throwable))}
                              :status 500))))))

(defn get-acc-number [request]
  (Long/parseLong (-> request :route-args :acc-number)))


; RESTful API ----------------------------------------------------

(defn get-test [request]
  (json-response request))

(defn get-balance [request]
  (json-response (bank/get-balance (get-acc-number request))))

(def bank-handler
  (route/routing
    ; curl -v http://localhost:3001/bank
    (route/with-route-matches :get "/" get-test)
    (route/with-route-matches :get "/:acc-number" get-balance) ; Error, is not taking the acc-number from request
    json-error-handler))


