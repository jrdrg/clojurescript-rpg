(ns rpg.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as async :refer [<! >! chan]])
  (:require-macros [cljs.core.async.macros :as am :refer [go-loop]]))

(enable-console-print!)


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
(def tile-size 25)

(def events-chan (chan))
  
(defn get-weighted-tiles []
  (flatten (map #(repeat (:weight %) %) tiles)))

(defn get-random-tile [tiles]
  (let [weights (rest (reductions #(+ %1 (:weight %2)) 0 tiles))
        rand (rand-int (last weights))]
    (nth tiles (count (take-while #(<= % rand) weights)))))

(defn get-random-map []
  (let [[width height] world-size
        weighted-tiles (get-weighted-tiles)
        random-tile (fn [] (rand-nth weighted-tiles))
        random-row (fn [] (vec (repeatedly width random-tile)))]
    (vec (repeatedly height random-row))
  ))

(defn get-tile [map x y]
  (get-in map [y x] (nth tiles 0)))

(defn update-state-new-map []
  (swap! game-state assoc-in [:world :tiles] (get-random-map)))


(defn run-events [in-chan]
  (go-loop [data (<! in-chan)]
    (print data)
    ))



(defn tile-view [{:keys [tile x y]}]
  [:div.tile {:style {:position "absolute"
                      :top (* y tile-size)
                      :left (* x tile-size)
                      :width tile-size
                      :height tile-size
                      :color (:color tile)
                      :background (:background tile)}}
   (:char tile)])


(defn map-view []
  (let [[sx sy] screen-size
        [pos-x pos-y] (get-in @game-state [:world :position])
        tile-map (get-in @game-state [:world :tiles])
        vx sx
        vy sy
        start-x pos-x
        start-y pos-y
        end-x (+ start-x vx)
        end-y (+ start-y vy)]
    [:div.map {:style {:width (* tile-size sx)
                       :height (* tile-size sy)}}
     (for [y (range start-y end-y) x (range start-x end-x)]
       ^{:key (str x ":" y)}
       [tile-view {:tile (get-tile tile-map x y)
                   :x (- x start-x)
                   :y (- y start-y)}])]))

(defn move-button [x y dir]
  (let [[pos-x pos-y] (get-in @game-state [:world :position])
        update-position (fn [] (swap! game-state assoc-in [:world :position] [(+ x pos-x) (+ y pos-y)]))]
    [:button {:on-click #(update-position)} dir]))
   
(defn map-ui-view []
  (update-state-new-map)
  (fn []
    [:div.container
     [map-view]
     [:button {:on-click #(update-state-new-map)} "new map"]
     [move-button -1 0 "Left"]
     [move-button 1 0 "Right"]
     [move-button 0 -1 "Up"]
     [move-button 0 1 "Down"]
     ]))

(defn app-view []
  (do
    (run-events events-chan))
  (fn []
    [:div.app
     [map-ui-view]
     ]))

(reagent/render-component [app-view]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
