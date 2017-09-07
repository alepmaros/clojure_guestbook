(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [guestbook.db.core :as db]
            [ring.util.response :refer [redirect]]
            [struct.core :as st]))

(def message-schema
    [[:name
      st/required
      st/string]

     [:message
      st/required
      st/string
      {:message "Mensagem deve ter pelo menos 5 caracteres"
       :validate #(> (count %) 4)}]])

(defn validate-message [params]
    (first (st/validate params message-schema)))

(defn save-message! [{:keys [params]}]
    (if-let [errors (validate-message params)]
        (-> (response/found "/")
            (assoc :flash (assoc params :errors errors)))
        (do
            (db/save-message!
                (assoc params :timestamp (java.util.Date.)))
            (response/found "/"))))

(defn home-page [{:keys [flash]}]
    (layout/render
        "home.html"
        (merge {:messages (db/get-messages)}
            (select-keys flash [:name :message :errors]))))

(defn about-page []
    (layout/render "about.html"))

(defroutes home-routes
    (GET "/" request (home-page request))
    (POST "/" request (save-message! request))
    (GET "/about" [] (about-page)))
