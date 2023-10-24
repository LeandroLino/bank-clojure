(ns bank.utils.core
  (:require [bank.db :refer [pg-db]]
            [clojure.java.jdbc :as j]))

(defn get-user [id]
  "Would recived a id string and return the data from users table"
  (first (j/query pg-db ["SELECT * FROM users WHERE id = ?" (Long/parseLong id)])))

(defn PersistentArrayMap->map [data]
  (zipmap (map keyword (keys data)) (vals data)))


(defn str->int [str]
  (Integer. (re-find #"[0-9]*" str)))