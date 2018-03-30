(ns gmail.client
  (:require [re-frame.core :as re-frame]
            [statecharts.path :as path]
            [cljs.core.async :as async :include-macros true]))

(def CLIENT_ID "945778004777-6vnl3erps8jbt94ulsc5o036kph66khj.apps.googleusercontent.com")
(def API_KEY "AIzaSyAve75w_Q-nF6w_WhmlUFQzA2vVIRRpUOQ")
(def DISCOVERY_DOCS ["https://www.googleapis.com/discovery/v1/apis/gmail/v1/rest"])
(def SCOPES "https://www.googleapis.com/auth/gmail.readonly")


(re-frame/reg-fx
  ::load-client
  (fn [_]
    (js/gapi.load "client:auth2" #(re-frame/dispatch [::client-loaded]))))

(re-frame/reg-fx
  ::init-client
  (fn [{:keys [client-id api-key discovery-docs scope] :as params}]
    (->
      (js/gapi.client.init
        (clj->js {:apiKey        API_KEY
                  :clientId      CLIENT_ID
                  :discoveryDocs DISCOVERY_DOCS
                  :scope         SCOPES}))
      (.then (fn []
               (-> (js/gapi.auth2.getAuthInstance)
                   (.-isSignedIn)
                   (.listen #(re-frame/dispatch [::signin-status-changed %])))

               (re-frame/dispatch [::signin-status-changed (-> (js/gapi.auth2.getAuthInstance)
                                                               (.-isSignedIn)
                                                               (.get))]))))))


(defn statechart [{:keys [client-id api-key discovery-docs scope] :as params}]
  {:type   :xor
   :init   ::loading
   :states {::loading      {:enter       [(fn [ctx]
                                            (assoc-in ctx [:fx ::load-client] {}))]
                            :transitions [{:event  ::client-loaded
                                           :target (path/sibling ::initializing)}]}
            ::initializing {:enter [(fn [ctx]
                                      (assoc-in ctx [:fx ::init-client] params))]}}})