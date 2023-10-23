(ns bank.core
  (:require [bank.server :refer [restart-server]])
  (:require [bank.tables :refer [users-table moviments-table]]))

(restart-server)
(users-table)
(moviments-table)