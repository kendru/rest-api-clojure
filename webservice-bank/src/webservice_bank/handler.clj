(ns webservice-bank.handler 
   (:require [compojure.core :refer :all]
             ;; :use is not commonly used anymore and can be replaced with a
             ;; ":refer :all" require. It's considered cleaner to alias every
             ;; require with ":as alias", as you have done with the rest of
             ;; the rest of the required namespaces. It minimizes the chance of
             ;; a name collision.
             [ring.util.response :refer :all]
             [compojure.handler :as handler]
             [ring.middleware.json :as middleware]
             [compojure.route :as route]
             [webservice-bank.bank :as bank]))

;; Since this middleware has a pretty clear single purpose, I would recommend
;; removing the exception-middleware-fn, as factoring it out does not make the
;; code clearer.
(defn wrap-exception-middleware [handler]
  "Wrap the exception treatment"
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        {:status 500
         ;; This is good idiomatic Clojure - it makes good use of apply
         ;; and the sequence abstraction without being too terse to understand.
         :body (apply str (interpose "\n" (.getStackTrace e)))}))))

(defn not-found-middleware [handler]
  "Treat not found endpoint"
 (fn [request]
   (or (handler request)
       ;; When you have a map with multiple entries, it is common coding style
       ;; To either separate each entry with a comma or to place each entry on
       ;; its own line. 
       {:status 404
        :body (str "404 Not Found:" (:uri request))})))

(defroutes app-routes
  ;curl -X GET  http://localhost:3001/balance/12345678
  (GET "/balance/:acc-number" [acc-number]
       (response {:acc-number acc-number
                  :balance (bank/get-balance (Long/parseLong acc-number))}))
  (route/resources "/"))


(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response
      not-found-middleware
      wrap-exception-middleware))













