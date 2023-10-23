(ns bank.jwt (:require
              [clj-jwt.core :as jwt]))

(def payload {:user-id 123 :roles ["admin" "user"]})
(def secret-key "my-secret-key")

(def token (jwt/jwt payload secret-key))
(prn payload)