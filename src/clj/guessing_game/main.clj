(ns guessing_game.main
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [joodo.middleware.asset-fingerprint :refer [wrap-asset-fingerprint]]
            [joodo.middleware.favicon :refer [wrap-favicon-bouncer]]
            [joodo.middleware.keyword-cookies :refer [wrap-keyword-cookies]]
            [joodo.middleware.request :refer [wrap-bind-request *request*]]
            [joodo.middleware.util :refer [wrap-development-maybe]]
            [joodo.middleware.view-context :refer [wrap-view-context]]
            [joodo.views :refer [render-template render-html *view-context*]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.head :refer [wrap-head]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.util.response :refer [redirect-after-post redirect response]]
            [shoreleave.middleware.rpc :refer [wrap-rpc]]))

(def start-attempts "8")
(def win "Perfect!")
(def low "Too Low!")
(def high "Too High!")
(def error "No funny bizness!")

(def too-high ["images/too_high/no.gif"
               "images/too_high/too_high.gif"
               "images/too_high/too_low.gif"
               "images/too_high/whoa_too_high.gif"])

(def too-low  ["images/too_low/nope.gif"
               "images/too_low/too_much_water.gif"
               "images/too_low/what_happens_to_me.gif"])

(def bingo    ["images/bingo/face_hugs.gif"
               "images/bingo/nailed_it.gif"])

(def funny-bizness ["images/funny_bizness/space_grumpy.gif"
                    "images/funny_bizness/attack_grumpy.gif"
                    "images/funny_bizness/walk_away.gif"])

(defn win? [result]
  (= win (:message result)))

(defn attempts-remaining [request]
  (:value (:attempts-remaining (:cookies request))))

(defn game-over? [request]
  (= 0 (attempts-remaining request)))

(defn attempts-remaining? [request]
  (if-let [value (:value (:attempts-remaining (:cookies request)))]
    (not (= 0 (Integer/parseInt value)))
    false))

(defn super-secret-number? [request]
  (if-let [super-secret-number (:value (:super-secret-number (:cookies request)))]
    true
    false))

(defn reset-attempts [request]
  (assoc-in request [:cookies :attempts-remaining :value] start-attempts))

(defn reset-super-secret-number [request]
  (assoc-in request [:cookies :super-secret-number :value] (str (rand-int 100))))

(defn game-over-response [result secret-number]
  {:status 200
   :body (render-template "game_over"
    :message (:message result)
    :image (:image result)
    :secret-number secret-number)
   :cookies {:attempts-remaining {:value 0}}})

(defn try-again-response [result request]
  {:status 200
   :body (render-template "guess"
          :attempts-remaining (attempts-remaining request)
          :result (:message result)
          :image (:image result))
   :cookies (:cookies request)})

(defn decrement-attempts-remaining [request]
  (let [new-attempts (dec (Integer/parseInt (:value (:attempts-remaining (:cookies request)))))]
    (assoc-in request [:cookies :attempts-remaining :value] new-attempts)))

(defn guess-result [guess super-secret-number]
  (try
    (cond
      (nil? guess) ""
      (= (Integer/parseInt guess) (Integer/parseInt super-secret-number)) {:message win  :image (nth bingo    (rand-int (count bingo)))}
      (> (Integer/parseInt guess) (Integer/parseInt super-secret-number)) {:message high :image (nth too-high (rand-int (count too-high)))}
      (< (Integer/parseInt guess) (Integer/parseInt super-secret-number)) {:message low  :image (nth too-low  (rand-int (count too-low)))})
  (catch Exception e
    {:message error :image (nth funny-bizness (rand-int (count funny-bizness)))})))

(defn process-guess [request]
  (let [decremented-request (decrement-attempts-remaining request)
        result (guess-result (:guess (:params request)) (:value (:super-secret-number (:cookies decremented-request))))]
    (if (or (game-over? decremented-request) (win? result))
      (game-over-response result (:value (:super-secret-number (:cookies decremented-request))))
      (try-again-response result decremented-request))))

(defn reset-game [request body]
  (-> (reset-attempts request)
      reset-super-secret-number
      body))

(defmacro ensure-cookie-values [request body]
  `(if (and (attempts-remaining? ~request) (super-secret-number? ~request))
     (~body ~request)
     (reset-game ~request ~body)))

(defroutes app-routes
  (GET "/" {:as request} (ensure-cookie-values request process-guess))
  (PUT "/" {:as request} (ensure-cookie-values request process-guess))
  (route/not-found (render-template "not_found" :template-root "guessing_game" :ns `guessing_game.view-helpers)))

(def app-handler
  (->
    app-routes
    (wrap-view-context :template-root "guessing_game" :ns `guessing_game.view-helpers)
    wrap-rpc))

(def app
  (-> app-handler
    wrap-development-maybe
    wrap-bind-request
    wrap-keyword-params
    wrap-params
    wrap-multipart-params
    wrap-flash
    wrap-keyword-cookies
    wrap-session
    wrap-cookies
    wrap-favicon-bouncer
    (wrap-resource "public")
    wrap-asset-fingerprint
    wrap-file-info
    wrap-head))
