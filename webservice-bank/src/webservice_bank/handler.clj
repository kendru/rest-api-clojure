(ns webservice-bank.handler
   (:require [webservice-bank.bank-handler :as bank-handler]
            [webservice-bank.route :as routee]
            [compojure.core :as compojure]))



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


(compojure/defroutes route-handler
  "Set all routes"
   (compojure/context "/bank" []
                     bank-handler/bank-handler))
    
  (def full-handler
    "Join all middlewares"
    (-> route-handler
      not-found-middleware
      wrap-exception-middleware))



