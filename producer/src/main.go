package main

import (
	"encoding/json"
	"fmt"
	"time"

	"gopkg.in/confluentinc/confluent-kafka-go.v1/kafka"
)

type Payload struct {
	SensorID string `json:"sensorId"`
	Content  string `json:"content"`
}

func main() {

	p, err := kafka.NewProducer(&kafka.ConfigMap{"bootstrap.servers": "host.docker.internal:9092"})
	if err != nil {
		panic(err)
	}

	defer p.Close()

	// Delivery report handler for produced messages
	go func() {
		for e := range p.Events() {
			switch ev := e.(type) {
			case *kafka.Message:
				if ev.TopicPartition.Error != nil {
					fmt.Printf("Delivery failed: %v\n", ev.TopicPartition)
				} else {
					fmt.Printf("Delivered message to %v\n", ev.TopicPartition)
				}
			}
		}
	}()

	// Produce messages to topic (asynchronously)
	topic := "sensorsTopic"
	for {
		for _, payload := range []Payload{Payload{SensorID: "AABB", Content: "15"}} {
			b, _ := json.Marshal(payload)

			p.Produce(&kafka.Message{
				TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
				Value:          b,
			}, nil)
			time.Sleep(1 * time.Second)

		}

		p.Flush(15 * 1000)
	}

	// Wait for message deliveries before shutting down

}
