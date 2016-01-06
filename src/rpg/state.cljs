(ns rpg.state
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce game-state (atom {:world {:tiles []
                                   :player-pos [0 0]
                                   :position [0 0]
                                   :enemies {}    ;we can use vectors like [x y] as keys, so 1 enemy per tile at a time
                                   :items {} ; same
                                   }
                           :current-ui :map
                           :player {:position [10 10]
                                    :hp 10
                                    :atk 1
                                    :def 1
                                    :inventory []
                                    :armor {:name "nothing" :def 1}
                                    :weapon {:name "fists" :atk 1}
                                    }
                           }))


(defn get-world-map []
  (get-in @game-state [:world :tiles]))

(defn get-map-scroll-position []
  (get-in @game-state [:world :position]))

(defn get-player-position []
  (get-in @game-state [:player :position]))
