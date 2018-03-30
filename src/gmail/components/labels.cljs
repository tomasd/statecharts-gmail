(ns gmail.components.labels
  (:require [statecharts.re-frame :as scr]
            [re-frame.core :as re-frame]))

(defn load-labels []
  (fn [ctx _]
    (scr/fx ctx :fx/load-labels
            {:success [:labels-loaded]})))

(def statechart
  {:enter       [(load-labels)]
   :exit        [(scr/ctx-dissoc-db-in [:labels])]
   :transitions [{:event   :labels-loaded
                  :execute [(fn [ctx [_ labels]]
                              (-> ctx
                                  (scr/assoc-db-in [:labels :id->label]
                                                   (->> labels
                                                        (map (juxt :id identity))
                                                        (into {})))
                                  (scr/assoc-db-in [:labels :type->label]
                                                   (->> labels
                                                        (sort-by :name)
                                                        (group-by :type)))))]}
                 {:event   :reload-labels
                  :execute [(load-labels)]}
                 {:event   :label-clicked
                  :execute [(fn [ctx [_ label-id]]
                              (scr/assoc-db-in ctx [:labels :selected] label-id))]}]})

(re-frame/reg-sub
  :labels
  (fn [db [_ type]]
    (get-in db [:labels :type->label type])))

(re-frame/reg-sub
  :label/selected?
  (fn [db [_ label-id]]
    (= label-id (get-in db [:labels :selected]))))

(defn labels-list [type]
  [:ul.list-unstyled
   (doall
     (for [{:keys [id name]} @(re-frame/subscribe [:labels type])]
       [:li {:key id}
        [:a {:href     "#"
             :class    (if @(re-frame/subscribe [:label/selected? id])
                         "badge badge-primary"
                         "badge badge-light")
             :on-click #(do
                          (re-frame/dispatch [:label-clicked id])
                          (.preventDefault %))}
         name]]))])