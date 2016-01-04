(ns rpg.map
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce game-state (atom {:world {:tiles []
                                   :position [0 0]
                                   }
                           :text "test"
                           }))

(def tile-data [[:bounds     "X"   "red"   "black" 0]
                [:floor      "."   "brown" "black" 8]
                [:tree       "T"   "green" "black" 2]
                [:mountain   "^"   "gray"  "black" 1]])

(defrecord Tile [key char color background weight])
(def tiles (map #(apply ->Tile %) tile-data))

(def screen-size [20 20])
(def world-size [50 50])

  
(defn get-weighted-tiles []
  (flatten (map #(repeat (:weight %) %) tiles)))

(defn get-random-tile [tiles]
  (let [weights (rest (reductions #(+ %1 (:weight %2)) 0 tiles))
        rand (rand-int (last weights))]
    (nth tiles (count (take-while #(<= % rand) weights)))))

(defn get-tile [map x y]
  (get-in map [y x] (nth tiles 0)))

(defn get-random-map []
  (let [[width height] world-size
        weighted-tiles (get-weighted-tiles)
        random-tile (fn [] (rand-nth weighted-tiles))
        random-row (fn [] (vec (repeatedly width random-tile)))]
    (vec (repeatedly height random-row))))

(defn update-state-new-map []
  (swap! game-state assoc-in [:world :tiles] (get-random-map)))

