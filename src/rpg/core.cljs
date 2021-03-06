(ns rpg.core
  (:require [reagent.core :as reagent :refer [atom]]
            [rpg.state :as state :refer [game-state]]
            [rpg.map :as map :refer [get-tile]]
            [cljs.core.async :as async :refer [<! >! chan put!]])
  (:require-macros [cljs.core.async.macros :as am :refer [go-loop]]))


(enable-console-print!)

(def tile-size 25)
(def events-chan (chan))

(defmacro handler-fn
  ([& body]
   `(fn [~'event] ~@body nil)))  ; always return nil


;; dispatch events by type
(defmulti dispatch-event #(:type %))

(defmethod dispatch-event :move [{:keys [type delta]}]
  (let [[pos-x pos-y] (get-in @game-state [:world :position])
        [x y] delta]
    (swap! game-state assoc-in [:world :position] [(+ pos-x x) (+ pos-y y)])))

(defmethod dispatch-event :player-move []
  )


;; main event loop
(defn run-events [in-chan]
  (go-loop []
    (let [data (<! in-chan)]
      (print data)
      (cond
        (:type data)
        (dispatch-event data))
      (recur))))


(defn tile-view [{:keys [tile x y]}]
  [:div.tile {:style {:position "absolute"
                      :top (* y tile-size)
                      :left (* x tile-size)
                      :width tile-size
                      :height tile-size
                      :color (:color tile)
                      :background (:background tile)}}
   (:char tile)])


(defn move-button [x y dir]
  (let [[pos-x pos-y] (get-in @game-state [:world :position])
        send-update-event #(put! events-chan {:type :move
                                              :delta [x y]})]
    [:button {:on-click #(send-update-event)} dir]))


(defn player-view []
  (let [[player-x player-y] (get-in @game-state [:player :position])]
    [:div.tile {:style {:color "white"
                        :position "absolute"
                        :top (* player-y tile-size)
                        :left (* player-x tile-size)
                        :width tile-size
                        :height tile-size
                   :text-align "center"}} "@"]))

(defn player-info-view []
  (let [{:keys [hp atk def weapon armor]} (get-in @game-state [:player])]
    [:div.player-info
     [:div (str "HP: " hp)]
     [:div (str "atk: " atk)]
     [:div (str "def: " def)]
     [:div (str "weapon: " (:name weapon))]
     [:div (str "armor: " (:name armor))]
     ]))


(defn map-view []
  (let [[sx sy] map/screen-size
        [pos-x pos-y] (state/get-map-scroll-position)
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
                   :y (- y start-y)}])
     [player-view]
     ]))

(defn map-ui-view []
  (map/update-state-new-map)
  (fn []
    [:div.container
     [:div.wrapper
      [map-view]
      [player-info-view]
      ]
     [:button {:on-click #(map/update-state-new-map)} "new map"]
     [move-button -1 0 "Left"]
     [move-button 1  0 "Right"]
     [move-button 0 -1 "Up"]
     [move-button 0  1 "Down"]
     ]))

(defn player-info-ui-view []
  [:div "Player/Inventory/etc"])

(defn app-view []
  (do
    (run-events events-chan))
  (fn []
    [:div.app
     (case (:current-ui @game-state)
       :map         [map-ui-view]
       :player-info [player-info-ui-view]
       [:div "this shouldn't be rendered"])
     ]))

(reagent/render-component [app-view]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
