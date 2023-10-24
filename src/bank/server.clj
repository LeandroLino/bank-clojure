(ns bank.server
  (:import [java.math BigDecimal])
  (:require
   [bank.controllers.core :as controllers]
   [io.pedestal.http.route :as route]
   [io.pedestal.http :as http]
   #_[io.pedestal.test :as test]
   [clojure.java.jdbc :as j]
   [ring.util.response :as ring-resp]
   [cheshire.core :as json]))

(defn list-users [request]
  (controllers/list-users request))

(defn health [request]
  (ring-resp/response "OK!"))

(defn create-user [request]
  (controllers/create-user request))

(defn delete [request]
  (controllers/delete-user request))

(defn update-user [request]
  (controllers/update-user request))

(defn moviments [request]
  (controllers/moviments request))

(def routes (route/expand-routes #{["/health-check" :get health :route-name :health-check]
                                   ["/create" :post create-user :route-name :create]
                                   ["/moviment" :post moviments :route-name :deposit]
                                   ["/delete" :delete delete :route-name :delete]
                                   ["/update" :put update-user :route-name :update]
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