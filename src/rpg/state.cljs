(ns rpg.state
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce game-state (atom {:world {:tiles []
                                   :player-pos [0 0]
                                   :position [0 0]}
                           :text "test"
                           :current-ui :map
                           }))


