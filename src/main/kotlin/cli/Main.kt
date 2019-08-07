package cli

import benchmark.LatencyAnalyzer
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findObject
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import simple.SimpleClient

class KafkaDemo : CliktCommand(help = "Kafka Examples") {
    private val brokers: String by option(help = "The Kafka Brokers").default("localhost:9092")
    private val config by findObject { mutableMapOf<String, String>() }

    override fun run() {
        config["BROKERS"] = brokers
        echo(brokers)
    }
}

class Simple : CliktCommand(help = "Run a simple Kafka Client") {
    private val consumer by option("-c").flag()
    private val producer by option("-p").flag()
    private val config by requireObject<Map<String, String>>()

    override fun run() {
        if (consumer) {
            config["BROKERS"]?.let { SimpleClient(it).process() }
        }
        if (producer) {
            config["BROKERS"]?.let { SimpleClient(it).produce(2) }
        }
    }
}

class Benchmark: CliktCommand(help = "Measure performance of a Kafka Broker") {
    private val inputTopic: String by option(help = "Topic to Read Messages from").default("persons")
    private val outputTopic: String by option(help = "Topic to Read Messages from").default("ages")
    private val config by requireObject<Map<String, String>>()

    override fun run() {
        config["BROKERS"]?.let { LatencyAnalyzer(it, inputTopic, outputTopic).start() }
    }
}

fun main(args: Array<String>) {
    KafkaDemo()
            .subcommands(Simple(), Benchmark())
            .main(args)
}