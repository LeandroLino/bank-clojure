(ns bank.server
  (:import [java.math BigDecimal])
  (:require [bank.db :refer [pg-db]]
            [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            #_[io.pedestal.test :as test]
            [clojure.java.jdbc :as j]
            [ring.util.response :as ring-resp]
            [cheshire.core :as json]
            [clj-jwt.core :as jwt]))

(defn get-user [id]
  "Would recived a id string and return the data from users table"
  (first (j/query pg-db ["SELECT * FROM users WHERE id = ?" (Long/parseLong id)])))

(defn PersistentArrayMap->map [data]
  (zipmap (map keyword (keys data)) (vals data)))

(defn str->int [str]
  (Integer. (re-find #"[0-9]*" str)))

(defn list-users [request]
  (let [id (-> request :query-params :id)
        users (if id
                (get-user id)
                (j/query pg-db ["SELECT * FROM users"]))]
    (ring-resp/response users)))

(defn health [request]
  (ring-resp/response "OK!"))

(defn create [request]
  (let [data (assoc (json/parse-string (slurp (:body request))) "balance" 0.0M)
        inserted-data (j/insert! pg-db :users data)]
    (ring-resp/response (first inserted-data))))

  (defn delete [request]
    (let [id
          (str->int (-> request :query-params :id))]
      (j/delete! pg-db :users ["id = ? " id])
      {:status 202}))

  (defn update [request]
    (let [id (str->int (-> request :params :id))
          data (json/parse-string (slurp (:body request)))]
      (j/update! pg-db :users data ["id = ?" id])
      (let [update-data (get-user (str id))]
        {:status 200 :body update-data})))

  (defn moviment [request]
    (let [body (PersistentArrayMap->map (json/parse-string (slurp (:body request))))
          headers (:headers request)
          amount (get-in body [:amount])]
      (if (number? amount)
        (do
          (let [user-id (str->int (get-in headers ["user-id"]))
                moviment {:type (if (> amount 0) "deposit" "withdraw")
                          :user_id user-id
                          :amount (BigDecimal. (str amount))}
                user-balance (get-user (get-in headers ["user-id"]))]
            (j/insert! pg-db :moviments moviment)
            (j/update! pg-db :users {:balance (.add (BigDecimal. (str (:balance user-balance))) (BigDecimal. (str (:amount moviment))))} ["id = ?" user-id]))
          (ring-resp/response (get-user (get-in headers ["user-id"]))))
        {:status 400 :body {:error true}})))

  (def routes (route/expand-routes #{["/health-check" :get health :route-name :health-check]
                                     ["/create" :post create :route-name :create]
                                     ["/moviment" :post moviment :route-name :deposit]
                                     ["/delete" :delete delete :route-name :delete]
                                     ["/update" :put update :route-name :update]
                                     ["/list" :get list-users :route-name :list]}))

  (def servide-map {::http/routes routes
                    ::http/port 9999
                    ::http/type :jetty
                    ::http/join? false})

  (defonce server (atom nil))

  (defn start-server []
    (reset! server (http/start (http/create-server servide-map))))

  (defn stop-server []
    (http/stop @server))

  (defn restart-server []
    (try
      (stop-server)
      (start-server)
      (catch Exception e
        (start-server))))

  (restart-server)


  (prn "-------------------")
  (prn "Server is running")
  (prn "in localhost:9999")
  (prn "-------------------")