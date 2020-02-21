from kafka import KafkaProducer
from kafka.errors import KafkaError
import time
import json
import random
producer = KafkaProducer(bootstrap_servers=['localhost:9092'])
msgs = [
    {"sensorID": "0101", "sensorType": "noise", "content": 38},
    {"sensorID": "0101", "sensorType": "noise", "content": 44},
    {"sensorID": "0101", "sensorType": "noise", "content": 98},
    {"sensorID": "0202", "sensorType": "light", "content": "on"},
    {"sensorID": "0202", "sensorType": "light", "content": "off"},
    {"sensorID": "0303", "sensorType": "wind", "content": 45},
    {"sensorID": "0303", "sensorType": "wind", "content": 10},
    {"sensorID": "0303", "sensorType": "wind", "content": 22},
]
# produce asynchronously
while True:
    r = random.choice(msgs)
    r = json.dumps(r)
    producer.send('myTopic', r.encode())
    # producer.flush()
    time.sleep(1)
