(ns bank.controllers.core
  (:import [java.math BigDecimal])
  (:require [bank.db :refer [pg-db]]
            [bank.utils.core :as utils]
            [bank.domains.core :as domains]
            [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            #_[io.pedestal.test :as test]
            [clojure.java.jdbc :as j]
            [ring.util.response :as ring-resp]
            [cheshire.core :as json]))

(defn list-users [request]
  (let [id (-> request :query-params :id)
        users (if id
                (domains/get-user id)
                (domains/get-users))]
    (ring-resp/response users)))

(defn create-user [request]
  (let [data (assoc (json/parse-string (slurp (:body request))) "balance" 0.0M)]
    (ring-resp/response (domains/create-user data))))

(defn delete-user [request]
  (let [id
        (utils/str->int (-> request :query-params :id))]
    (domains/delete-user id)
    {:status 202}))

(defn update-user [request]
  (let [id (utils/str->int (-> request :params :id))
        data (json/parse-string (slurp (:body request)))]
    (let [update-data (domains/update-user id data)]
      {:status 200 :body update-data})))

(defn moviments [request]
  (let [body (utils/PersistentArrayMap->map (json/parse-string (slurp (:body request))))
        headers (:headers request)
        amount (get-in body [:amount])]
    (if (number? amount)
      (do
        (domains/moviments headers amount)
         (ring-resp/response (domains/get-user (get-in headers ["user-id"]))))
      {:status 400 :body {:error true}})))
