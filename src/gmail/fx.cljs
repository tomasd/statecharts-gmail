(ns gmail.fx
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-fx
  :fx/sign-in
  (fn [_]
    (-> (js/gapi.auth2.getAuthInstance)
        (.signIn))))

(re-frame/reg-fx
  :fx/sign-out
  (fn [_]
    (-> (js/gapi.auth2.getAuthInstance)
        (.signOut))))

(re-frame/reg-fx
  :fx/load-labels
  (fn [{:keys [success]}]
    (-> (js/gapi.client.gmail.users.labels.list #js {"userId" "me"})
        (.then (fn [response]
                 (let [labels (-> response .-result .-labels)]
                   (re-frame/dispatch (conj success (js->clj labels :keywordize-keys true)))))))))



(defn load-threads [{:keys [query success label-ids]}]
  (-> (js/gapi.client.gmail.users.threads.list #js {"userId"   "me"
                                                    "q"        query
                                                    "labelIds" (clj->js label-ids)})
      (.then (fn [response]
               (let [result  (js->clj (-> response .-result) :keywordize-keys true)
                     threads (->> (:threads result)
                                  (map (juxt :id identity))
                                  (into {}))]
                 (re-frame/dispatch (conj success (->> (:threads result)
                                                       (mapv #(get threads (:id %)))))))))))

(re-frame/reg-fx
  :fx/load-threads
  (fn [{:keys [label-ids query success]}]
    (load-threads {:query     query
                   :label-ids label-ids
                   :success   success})))

(defn load-thread [id success]
  (-> (js/gapi.client.gmail.users.threads.get #js {"userId" "me"
                                                   "id"     id})
      (.then (fn [response]
               (let [thread (js->clj (-> response .-result) :keywordize-keys true)]
                 (re-frame/dispatch (conj success thread))
                 )))))
(re-frame/reg-fx
  :fx/load-thread
  (fn [{:keys [thread-id success]}]
    (load-thread thread-id success)))

(re-frame/reg-fx
  :fx/set-url
  (fn [url]
    (set! (.-hash js/document.location) url)))