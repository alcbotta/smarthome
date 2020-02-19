from kafka import KafkaProducer
from kafka.errors import KafkaError
import time

producer = KafkaProducer(bootstrap_servers=['localhost:9092'])

# produce asynchronously
for _ in range(10000):
    producer.send('myTopic', b'msg')
    # producer.flush()
    time.sleep(1)
