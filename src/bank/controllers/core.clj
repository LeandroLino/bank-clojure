(ns bank.controllers.core
  (:require [bank.db :refer [pg-db]]
            [bank.utils.core :as utils]
            [bank.domains.core :as domains]
            #_[io.pedestal.test :as test]
            [clojure.java.jdbc :as j]
            [cheshire.core :as json]))

(defn list-users [request]
  (let [id (-> request :query-params :id)
        users (if id
                (domains/get-user id)
                (domains/get-users))] users))

(defn create-user [request]
  (let [data (assoc (json/parse-string (slurp (:body request))) "balance" 0.0M)]
    (domains/create-user data)))

(defn delete-user [request]
  (let [id
        (utils/str->int (-> request :query-params :id))]
    (domains/delete-user id)
    {:status 202}))

(defn update-user [request]
  (let [id (utils/str->int (-> request :params :id))
        data (json/parse-string (slurp (:body request)))]
    (let [update-data (domains/update-user id data)]
      update-data)))

(defn moviments [request]
  (let [body (utils/PersistentArrayMap->map (json/parse-string (slurp (:body request))))
        headers (:headers request)
        amount (get-in body [:amount])]
    (if (number? amount)
      (do
        (domains/moviments headers amount)
        (domains/get-user (get-in headers ["user-id"])))
      {:status 400 :body {:error true}})))

(defn extract [request]
  (let [headers (:headers request)
        data (domains/get-moviments (get-in headers ["user-id"]))]
    data))

(defn transfer [request]
  (let [amount (get-in (json/parse-string (slurp (:body request))) ["amount"])
        from-user-id (:from (:query-params request))
        to-user-id (:to (:query-params request))
        from-user (domains/get-user from-user-id)
        to-user (domains/get-user to-user-id)]
    (domains/update-user (utils/str->int from-user-id) (assoc from-user :balance (- (:balance from-user) amount)))
    (let [update-to-user
          (domains/update-user (utils/str->int to-user-id) (assoc to-user :balance (+ (:balance to-user) amount)))]
      update-to-user)))