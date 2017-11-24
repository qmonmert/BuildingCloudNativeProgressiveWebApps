package fr.ippon.bikecatalogservice

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@SpringBootApplication
class BikeCatalogServiceApplication

fun main(args: Array<String>) {
    runApplication<BikeCatalogServiceApplication>(*args)
}

//fun main(args: Array<String>) {
//    SpringApplicationBuilder()
//            .sources(BikeCatalogServiceApplication::class.java)
//            .initializers(beans {
//
//            })
//            .run(*args)
//}

@Component
class SampleBikeRepository(private val bikeRepository : BikeRepository) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {

        val bikes = Flux.just("Trek", "Canyon", "Giant")
                .map { Bike(name = it) }
                .flatMap { bikeRepository.save(it) }

        bikeRepository
                .deleteAll()
                .thenMany(bikes)
                .thenMany(bikeRepository.findAll())
                .subscribe { println(it) }
    }

}

interface BikeRepository : ReactiveMongoRepository<Bike, String>

@Document
data class Bike(@Id var id: String? = null, var name: String? = null)
