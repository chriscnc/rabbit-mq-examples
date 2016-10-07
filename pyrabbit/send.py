#!/usr/bin/env python
import pika

connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))

channel = connection.channel()
channel.queue_declare(queue='langohr.examples.hello-world')

channel.basic_publish(exchange='',
                      routing_key='langohr.examples.hello-world',
                      body='Hello World!')

print(" [x] Sent 'Hello World!'")
connection.close()
