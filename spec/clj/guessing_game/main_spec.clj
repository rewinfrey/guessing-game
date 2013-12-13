(ns guessing_game.main-spec
  (:require [speclj.core :refer :all]
            [joodo.spec-helpers.controller :refer :all]
            [guessing_game.main :refer :all]))

(describe "guessing_game"

  (with-mock-rendering)
  (with-routes app-handler)

  (it "handles root"
    (let [result (do-get "/")]
      (should= 200 (:status result))
      (should= "index" @rendered-template)))
  )

(run-specs)
