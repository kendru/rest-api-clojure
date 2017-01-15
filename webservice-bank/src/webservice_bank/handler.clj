(ns webservice-bank.handler
   (:use ring.util.response)
   (:require [compojure.core :refer :all]
             [compojure.handler :as handler]
             [ring.middleware.json :as middleware]
             [compojure.route :as route]
             [webservice-bank.bank :as bank]))

(defn exception-middleware-fn [handler request]
 "Treat exception"
 (try (handler request)
  (catch Throwable e 
   {:status 500 :body (apply str (interpose "\n" (.getStackTrace e)))})))

(defn wrap-exception-middleware [handler]
  "Wrap the exception treatment"
 (fn [request]
  (exception-middleware-fn handler request)))

(defn not-found-middleware [handler]
  "Treat not found endpoint"
 (fn [request]
  (or (handler request)
   {:status 404 :body (str "404 Not Found:" (:uri request))})))

(defroutes app-routes
  ;curl -X GET  http://localhost:3001/balance/12345678
  (GET "/balance/:acc-number" [acc-number]
       (response {:acc-number acc-number :balance (bank/get-balance (Long/parseLong acc-number))}))
  (route/resources "/"))


(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response
      not-found-middleware
      wrap-exception-middleware))













