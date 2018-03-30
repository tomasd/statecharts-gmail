(ns gmail.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            gmail.fx
            [statecharts.core :as sc]
            [statecharts.re-frame :as scr]
            [gmail.config :as config]
            [gmail.client :as client]
            [statecharts.path :as path]
            [gmail.anonymous :as anonymous]
            [gmail.authenticated :as authenticated]))

(def CLIENT_ID "945778004777-6vnl3erps8jbt94ulsc5o036kph66khj.apps.googleusercontent.com")
(def API_KEY "AIzaSyAve75w_Q-nF6w_WhmlUFQzA2vVIRRpUOQ")
(def DISCOVERY_DOCS ["https://www.googleapis.com/discovery/v1/apis/gmail/v1/rest"])
(def SCOPES "https://www.googleapis.com/auth/gmail.readonly")


(def statechart
  (sc/make
    {:type        :xor
     :init        :check-auth-status
     :enter       [(scr/ctx-assoc-db-in [:user-status] :loading)]
     :states      {:check-auth-status (client/statechart {:api-key        API_KEY
                                                          :client-id      CLIENT_ID
                                                          :discovery-docs DISCOVERY_DOCS
                                                          :scope          SCOPES})
                   :authenticated     authenticated/statechart
                   :anonymous         anonymous/statechart}
     :transitions [{:event  :restart-app
                    :target (path/child [:check-auth-status ::client/initializing])}
                   {:event     :gmail.client/signin-status-changed
                    :condition (fn [ctx [_ signed-in?]]
                                 signed-in?)
                    :target    :authenticated}
                   {:event     :gmail.client/signin-status-changed
                    :condition (fn [ctx [_ signed-in?]]
                                 (not signed-in?))
                    :target    :anonymous}]}))

(re-frame/reg-sub
  :user-status
  (fn [db]
    (:user-status db)))

(defn main-page []
  [:div

   (case @(re-frame/subscribe [:user-status])
     :authenticated [authenticated/page]
     :anonymous [anonymous/page]
     :loading [:div "Loading..."])])

(re-frame/reg-event-fx
  :initialize-db
  (fn [_ _]
    (-> {:db {}}
        (scr/initialize statechart))))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-page]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (mount-root)
  (re-frame/dispatch [:restart-app]))