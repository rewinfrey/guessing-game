(ns guessing_game.init
  (:require [joodo.env :as env]))

(defn init []
  (env/load-configurations))
