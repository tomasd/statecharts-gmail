(ns gmail.anonymous
  (:require [statecharts.re-frame :as scr]
            [re-frame.core :as re-frame]))


(def statechart
  {:enter       [(scr/ctx-assoc-db-in [:user-status] :anonymous)]
   :transitions [{:event   :sign-in
                  :execute [(fn [ctx _]
                              (assoc-in ctx [:fx :fx/sign-in] {}))]}]})

(defn page []
  [:div.container
   [:button.btn.btn-primary
    {:type     :button
     :on-click #(re-frame/dispatch [:sign-in])}
    "Authorize"]])