package fr.ippon.bikecatalogservice

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

@SpringBootApplication
class BikeCatalogServiceApplication

fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .sources(BikeCatalogServiceApplication::class.java)
            .initializers(beans {

                bean {
                    val bikeRepository = ref<BikeRepository>()
                    router {
                        GET("/bikes") {
                            ServerResponse.ok().body(bikeRepository.findAll())
                        }
                    }
                }

                bean {
                    ApplicationRunner {
                        val bikeRepository = ref<BikeRepository>()
                        val bikes = Flux.just("Trek SL5", "Canyon", "Giant", "Trek SL6")
                                .map { Bike(name = it) }
                                .flatMap { bikeRepository.save(it) }
                        bikeRepository
                                .deleteAll()
                                .thenMany(bikes)
                                .thenMany(bikeRepository.findAll())
                                .subscribe { println(it) }
                    }
                }

            })
            .run(*args)
}

interface BikeRepository : ReactiveMongoRepository<Bike, String>

@Document
data class Bike(@Id var id: String? = null, var name: String? = null)
