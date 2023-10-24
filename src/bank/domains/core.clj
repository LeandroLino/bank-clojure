(ns bank.domains.core
  (:require [bank.db :refer [pg-db]]
            [bank.utils.core :as utils]
            [clojure.java.jdbc :as j]
            [cheshire.core :as json]))

(defn get-user [id]
  "Would recived a id string and return the data from an user table"
  (first (j/query pg-db ["SELECT * FROM users WHERE id = ?" (Long/parseLong id)])))

(defn get-users []
  "Would return the data from users table"
  (j/query pg-db ["SELECT * FROM users"]))

(defn create-user [data]
  (let [inserted-data (j/insert! pg-db :users data)]
    (first inserted-data)))

(defn delete-user [id]
  (j/delete! pg-db :users ["id = ? " id]))

(defn update-user [id data]
  (j/update! pg-db :users data ["id = ?" id])
  (let [update-data (get-user (str id))]
    update-data))

(defn moviments [headers amount]
  (let [user-id (utils/str->int (get-in headers ["user-id"]))
        moviment {:type (if (> amount 0) "deposit" "withdraw")
                  :user_id user-id
                  :amount (BigDecimal. (str amount))}
        user-balance (get-user (get-in headers ["user-id"]))]
    (j/insert! pg-db :moviments moviment)
    (j/update! pg-db :users {:balance (.add (BigDecimal. (str (:balance user-balance))) (BigDecimal. (str (:amount moviment))))} ["id = ?" user-id]))
  (get-user (get-in headers ["user-id"])))