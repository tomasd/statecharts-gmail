(ns gmail.authenticated
  (:require [statecharts.re-frame :as scr]
            [re-frame.core :as re-frame]

            [gmail.pages.threads :as threads]
            [statecharts.path :as path]))




(def statechart
  {:type   :and
   :states {:page {:type        :xor
                   :init        :dispatch-url
                   :states      {:dispatch-url {:enter [(fn [ctx _]
                                                          (let [url js/document.location.hash
                                                                [_ thread-id] (re-find #"#/messages/(.+)" url)]
                                                            (if (some? thread-id)
                                                              (scr/push-event ctx [:show-thread thread-id])
                                                              (scr/push-event ctx [:show-threads]))))]}
                                 :threads      threads/statechart}
                   :transitions [{:event  :show-thread
                                  :target (path/child [:threads :threads :detail])}
                                 {:event  :show-threads
                                  :target (path/child [:threads])}
                                 ]}}})



(defn page []
  [:div.container
   [:button.btn.btn-primary
    {:type     :button
     :on-click #(re-frame/dispatch [:sign-out])}
    "Sign out"]
   [threads/page]])
