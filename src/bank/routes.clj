(ns bank.routes
  (:require
   [bank.controllers.core :as controllers]
   [io.pedestal.http.route :as route]
   [ring.util.response :as ring-resp]
   [cheshire.core :as json]))

(defn list-users [request]
  (ring-resp/response (controllers/list-users request)))

(defn health [request]
  (ring-resp/response "OK!"))

(defn create-user [request]
  (ring-resp/response (controllers/create-user request)))

(defn delete [request]
  (ring-resp/response (controllers/delete-user request)))

(defn update-user [request]
  (ring-resp/response (controllers/update-user request)))

(defn moviments [request]
  (ring-resp/response (controllers/moviments request)))

(defn extract [request]
  (ring-resp/response (controllers/extract request)))

(defn transfer [request]
  (ring-resp/response (controllers/transfer request)))


(def routes (route/expand-routes #{["/health-check" :get health :route-name :health-check]
                                   ["/create" :post create-user :route-name :create]
                                   ["/moviment" :post moviments :route-name :deposit]
                                   ["/extract" :get extract :route-name :extract]
                                   ["/transfer" :post transfer :route-name :transfer]
                                   ["/delete" :delete delete :route-name :delete]
                                   ["/update" :put update-user :route-name :update]
                                   ["/list" :get list-users :route-name :list]}))
