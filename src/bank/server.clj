(ns bank.server
  (:require
   [bank.controllers.core :as controllers]
   [io.pedestal.http :as http]
   [bank.routes :refer [routes]]
   #_[io.pedestal.test :as test]
   [clojure.java.jdbc :as j]
   [ring.util.response :as ring-resp]))

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