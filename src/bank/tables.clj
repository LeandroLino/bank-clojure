(ns bank.tables
  (:require [clojure.java.jdbc :as jdbc]
            [bank.db :refer [pg-db]]))

(defn users-table []
  (jdbc/execute! pg-db
    "CREATE TABLE IF NOT EXISTS users (
       id SERIAL PRIMARY KEY,
       name VARCHAR(500),
       document VARCHAR(500) UNIQUE,
       balance DECIMAL(10, 2))"))

(defn moviments-table []
  (jdbc/execute! pg-db
    "CREATE TABLE IF NOT EXISTS moviments (
       id SERIAL PRIMARY KEY,
       type VARCHAR(8) CHECK (type IN ('deposit', 'withdraw')),
       user_id INT REFERENCES users (id),
       amount DECIMAL(10, 2))"))