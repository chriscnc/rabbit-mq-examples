(ns clj-rabbit.core
  (:require 
    [langohr.core :as rmq]
    [langohr.channel :as lch]
    [langohr.queue :as lq]
    [langohr.consumers :as lc]
    [langohr.basic :as lb]
    [clojure.tools.logging :as log])
  (:import (java.util.concurrent Executors TimeUnit))
  (:gen-class))

(def ^{:const true} default-exchange-name "")

(defn make-generator
  [rmq-conn qname]
  (cast Callable
        (fn [] 
          (log/info "Starting sender")
          (let [ch    (lch/open rmq-conn)]
            (log/info (format "[sender] Connected. Channel id: %d" 
                              (.getChannelNumber ch)))
            (lq/declare ch qname)
            (loop [i 0]
              (log/info (format "[sender] Sending: %d" i))
              (lb/publish ch default-exchange-name qname 
                          (format "Message %d" i)
                          {:content-type "text/plain" 
                           :type "greetings.hi"})
              (Thread/sleep 1000)
              (recur (inc i)))))))


(defn handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (log/info 
    (format "[consumer] Receieved a message: %s, delivery tag: %d, content type: %s, type: %s"
            (String. payload "UTF-8") 
            delivery-tag 
            content-type 
            type)))


(defn make-receiver
  [rmq-conn handler-fn qname]
  (let [ch (lch/open rmq-conn)]
    (cast Callable
          (fn []
            (log/info "Starting receiver")
            (lq/declare ch qname)
            (lc/subscribe ch qname handler-fn)))))

;; Two kinds of workers
;; - Generators - a process that only sends messages not recieve them
;; - Receivers - a process that receives work from the queue and could
;;               possibly send new messages as a result
;; The question now is how to completely abstract what the workers do
;; from the machinery of wiring them to queues.

(defn startup 
  []
  (log/info "Connecting to RabbitMQ")
  (let [rmq-conn (rmq/connect)
        pool     (Executors/newFixedThreadPool 2)
        qname    "langohr.examples.hello-world"]
    (.submit pool (make-generator rmq-conn qname))
    (.submit pool (make-receiver rmq-conn handler qname))
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn [] 
                                 (log/info "Disconnecting from RabbitMQ")
                                 (rmq/close rmq-conn))))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (try
    (startup)
    (catch Exception e
      (log/error e)
      (System/exit 1))))


