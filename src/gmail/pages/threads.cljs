(ns gmail.pages.threads
  (:require [gmail.components.labels :as labels]
            [gmail.components.threads :as threads]
            [statecharts.re-frame :as scr]))

(def statechart
  {:type   :and
   :states {:labels  labels/statechart
            :threads threads/statechart
            :auth    {:enter       [(scr/ctx-assoc-db-in [:user-status] :authenticated)]
                      :transitions [{:event   :sign-out
                                     :execute [(fn [ctx _]
                                                 (assoc-in ctx [:fx :fx/sign-out] {}))]}]}}})

(defn page []
  [:div.row
   [:div.col-3
    [labels/labels-list "system"]
    [labels/labels-list "user"]]
   [:div.col-9
    [threads/threads-list]]])