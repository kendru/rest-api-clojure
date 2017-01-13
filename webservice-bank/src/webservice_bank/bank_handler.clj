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

(defn get-id [request]
  (Long/parseLong (-> request :route-args :id)))


; RESTful API ----------------------------------------------------

(defonce BLOG (atom {}))

(defonce ID (atom 0))

(defn get-blog-entries []
  (sort :id (vals BLOG)))

(defn add-blog-entry [entry]
  (let [id (swap! ID inc)]
    (get (swap! BLOG assoc id (assoc entry :id id)) id)))

(defn get-blog-entry [id]
  (get @BLOG id))

(defn update-blog-entry [id entry]
  (when (get-blog-entry id)
    (get (swap! BLOG assoc id entry) id)))

(defn alter-blog-entry [id entry-values]
  (when (get-blog-entry id)
    (get (swap! BLOG update-in [id] merge entry-values) id)))

(defn delete-blog-entry [id]
  (when (get-blog-entry id)
    (swap! BLOG dissoc id)
    {:id id}))


(defn get-handler [request]
  (json-response (get-blog-entries)))

(defn post-handler [request]
  (json-response (add-blog-entry (json-body request))))

(defn get-entry-handler [request]
  (json-response (get-blog-entry (get-id request))))

(defn put-handler [request]
  (json-response (update-blog-entry (get-id request) (json-body request))))

(defn delete-handler [request]
  (json-response (delete-blog-entry (get-id request))))

(def bank-handler
  (route/routing
    (route/with-route-matches :get "/" get-handler)
    ; curl -v http://localhost:3001/bank
    (route/with-route-matches :post "/" post-handler) 
    ; curl -v -X POST -d '{"title":"Book Title"}' -H "Content-Type: application/json" http://localhost:3001/bank
    (route/with-route-matches :get "/:id" get-entry-handler)
    (route/with-route-matches :put "/:id" put-handler)
    ; curl -X DELETE -v http://localhost:3001/bank/1
    (route/with-route-matches :delete "/:id" delete-handler)
    json-error-handler))

