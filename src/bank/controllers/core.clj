(ns bank.controllers.core
  (:import [java.math BigDecimal])
  (:require [bank.db :refer [pg-db]]
            [bank.utils.core :as utils]
            [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            #_[io.pedestal.test :as test]
            [clojure.java.jdbc :as j]
            [ring.util.response :as ring-resp]
            [cheshire.core :as json]))

(defn list-users [request]
  (let [id (-> request :query-params :id)
        users (if id
                (utils/get-user id)
                (j/query pg-db ["SELECT * FROM users"]))]
    (ring-resp/response users)))

(defn create-user [request]
  (let [data (assoc (json/parse-string (slurp (:body request))) "balance" 0.0M)
        inserted-data (j/insert! pg-db :users data)]
    (ring-resp/response (first inserted-data))))

(defn delete-user [request]
  (let [id
        (utils/str->int (-> request :query-params :id))]
    (j/delete! pg-db :users ["id = ? " id])
    {:status 202}))

(defn update-user [request]
  (let [id (utils/str->int (-> request :params :id))
        data (json/parse-string (slurp (:body request)))]
    (j/update! pg-db :users data ["id = ?" id])
    (let [update-data (utils/get-user (str id))]
      {:status 200 :body update-data})))

(defn moviments [request]
  (let [body (utils/PersistentArrayMap->map (json/parse-string (slurp (:body request))))
        headers (:headers request)
        amount (get-in body [:amount])]
    (if (number? amount)
      (do
        (let [user-id (utils/str->int (get-in headers ["user-id"]))
              moviment {:type (if (> amount 0) "deposit" "withdraw")
                        :user_id user-id
                        :amount (BigDecimal. (str amount))}
              user-balance (utils/get-user (get-in headers ["user-id"]))]
          (j/insert! pg-db :moviments moviment)
          (j/update! pg-db :users {:balance (.add (BigDecimal. (str (:balance user-balance))) (BigDecimal. (str (:amount moviment))))} ["id = ?" user-id]))
        (ring-resp/response (utils/get-user (get-in headers ["user-id"]))))
      {:status 400 :body {:error true}})))
