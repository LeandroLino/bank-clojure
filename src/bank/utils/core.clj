(ns bank.utils.core
  (:require [bank.db :refer [pg-db]]
            [clojure.java.jdbc :as j]))

(defn PersistentArrayMap->map [data]
  (zipmap (map keyword (keys data)) (vals data)))


(defn str->int [str]
  (Integer. (re-find #"[0-9]*" str)))