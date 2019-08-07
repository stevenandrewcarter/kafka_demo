package simple

import com.github.javafaker.Faker
import model.Person
import model.agesTopic
import model.jsonMapper
import model.personsTopic
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.LogManager
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*

class SimpleClient(brokers: String) {
    private val logger = LogManager.getLogger(javaClass)
    private val consumer = createConsumer(brokers)
    private val producer = createProducer(brokers)

    private fun createConsumer(brokers: String): Consumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["group.id"] = "person-processor"
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java
        return KafkaConsumer(props)
    }

    private fun createProducer(brokers: String): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer(props)
    }

    fun process() {
        consumer.subscribe(listOf(personsTopic))

        logger.info("Consuming and processing data")

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            logger.info("Received ${records.count()} records")

            records.iterator().forEach {
                val personJson = it.value()
                logger.debug("JSON data: $personJson")

                val person = jsonMapper.readValue(personJson, Person::class.java)
                logger.debug("Person: $person")

                val birthDateLocal = person.birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val age = Period.between(birthDateLocal, LocalDate.now()).getYears()
                logger.debug("Age: $age")

                val future = producer.send(ProducerRecord(agesTopic, "${person.firstName} ${person.lastName}", "$age"))
                future.get()
            }
        }
    }

    fun produce(ratePerSecond: Int) {
        val waitTimeBetweenIterationsMs = 1000L / ratePerSecond
        logger.info("Producing $ratePerSecond records per second (1 every ${waitTimeBetweenIterationsMs}ms)")

        val faker = Faker()
        while (true) {
            val fakePerson = Person(
                    firstName = faker.name().firstName(),
                    lastName = faker.name().lastName(),
                    birthDate = faker.date().birthday()
            )
            logger.info("Generated a person: $fakePerson")

            val fakePersonJson = model.jsonMapper.writeValueAsString(fakePerson)
            logger.debug("JSON data: $fakePersonJson")

            val futureResult = producer.send(ProducerRecord(personsTopic, fakePersonJson))
            logger.debug("Sent a record")

            Thread.sleep(waitTimeBetweenIterationsMs)

            // wait for the write acknowledgment
            futureResult.get()
        }
    }
}
