(ns gmail.components.threads
  (:require [statecharts.re-frame :as scr]
            [gmail.components.thread :as thread]
            [re-frame.core :as re-frame]
            [statecharts.path :as path]))



(def statechart
  {:type        :xor
   :enter       [(fn [ctx _]
                   (scr/fx ctx :fx/load-threads {:success [:threads-loaded]}))]
   :exit        [(scr/ctx-dissoc-db-in [:threads-mode])
                 (scr/ctx-dissoc-db-in [:threads])]
   :init        :list
   :states      {:list   {:enter [(scr/ctx-assoc-db-in [:threads-mode] :list)
                                  (scr/ctx-fx :fx/set-url "/messages")]}
                 :detail (thread/statechart
                           {:enter       [(scr/ctx-assoc-db-in [:threads-mode] :detail)
                                          (fn [ctx [_ thread-id]]
                                            (scr/fx ctx :fx/set-url (str "/messages/" thread-id)))]
                            :transitions [{:event  :label-clicked
                                           :target (path/sibling :list)}]
                            })}
   :transitions [{:event   :threads-loaded
                  :execute [(fn [ctx [_ page]]
                              (-> ctx
                                  (scr/assoc-db-in [:threads] page)))]}
                 {:event    :show-thread
                  :internal true
                  :target   :detail}
                 {:event    :show-threads
                  :internal true
                  :target   :list}
                 {:event   :label-clicked
                  :execute [(fn [ctx [_ label-id]]
                              (-> ctx
                                  #_(scr/assoc-db-in [:threads :label-ids] [label-id])
                                  (scr/fx :fx/load-threads
                                          {:label-ids [label-id]
                                           :success   [:threads-loaded]})))]}
                 {:event   :reload-threads
                  :execute [(fn [ctx _]
                              (-> ctx
                                  (scr/fx :fx/load-threads
                                          {:label-ids (scr/get-db-in ctx [:threads :label-ids])
                                           :success   [:threads-loaded]})))]}]})


(re-frame/reg-sub
  :threads
  (fn [db]
    (get-in db [:threads])))

(re-frame/reg-sub
  :threads-mode
  (fn [db]
    (get-in db [:threads-mode])))

(defn threads-list []
  (case @(re-frame/subscribe [:threads-mode])
    :list
    [:div
     [:button {:type     :button
               :on-click #(re-frame/dispatch [:reload-threads])}
      "Reload"]
     [:table.table.table-sm
      [:tbody
       (doall
         (for [{:keys [id snippet] :as thread} @(re-frame/subscribe [:threads])]
           [:tr {:key id}
            [:td snippet]
            [:td [:a {:href     "#"
                      :on-click #(do
                                   (re-frame/dispatch [:show-thread id])
                                   (.preventDefault %))}
                  "Show"]]]))]]]

    :detail
    [thread/current-thread [:show-threads]]

    [:div "Loading..."]))