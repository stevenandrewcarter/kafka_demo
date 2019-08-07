# Simple Kafka Example

This is a Simple Example of using Kafka with Kotlin. All it does is populate Topics with Data. It
shows a little bit of Data Manipulation by processing values from the first Topic and putting it on
the second topic.

## Usage

```$bash
kafkademo [OPTIONS] COMMAND [ARGS]
```

## Building

The `build.gradle` contains all of the information required for building the solution.

# Kafkacat

You can also use Kafkacat to test calls against kafka brokers. The following are some examples of using Kafkacat

```$bash
# List Meta Data
$ kafkacat -L -b localhost:9092
# Read from a Topic
$ kafkacat -C -b localhost:9092 -t <TOPIC_NAME>
# Write to a Topic
$ kafkacat -P -b localhost:9092 -t <TOPIC_NAME>
```
