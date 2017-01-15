(ns webservice-bank.handler
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
  (POST "/" request
    (let [name (or (get-in request [:params :name])
                   (get-in request [:body :name])
                   "John Doe")]
      {:status 200
       :body {:name name
       :desc (str "The name you sent to me was " name)}}))
    (GET "/soma" request 
         (prn "aqui")) ; curl -X GET  http://localhost:3001/soma
  (route/resources "/"))


(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response
      not-found-middleware
      wrap-exception-middleware))













