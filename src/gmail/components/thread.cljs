(ns gmail.components.thread
  (:require [statecharts.re-frame :as scr]
            [re-frame.core :as re-frame]))



(defn statechart [{:keys [enter transitions]}]
  {:enter       (-> [(fn [ctx [_ thread-id]]
                       (scr/fx ctx :fx/load-thread
                               {:thread-id thread-id
                                :success   [:thread-loaded thread-id]}))]
                    (into enter))
   :exit        [(scr/ctx-dissoc-db-in [:current-thread])]
   :transitions (-> [{:event   :thread-loaded
                      :execute [(fn [ctx [_ thread-id thread]]
                                  (-> ctx
                                      (scr/assoc-db-in [:current-thread] thread)))]}]
                    (into transitions))})


(re-frame/reg-sub
  :current-thread
  (fn [db]
    (get-in db [:current-thread])))

(defn current-thread [back-cmd]
  [:div
   [:a {:href     "#"
        :on-click #(do
                     (re-frame/dispatch back-cmd)
                     (.preventDefault %))}
    "<- Back"]
   [:ul
    (doall
      (for [message (:messages @(re-frame/subscribe [:current-thread]))]
        [:li {:key (:id message)}
         (:snippet message)]))
    ]])