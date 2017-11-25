package fr.ippon.edgeservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerExchangeFilterFunction
import org.springframework.cloud.gateway.route.gateway
import org.springframework.cloud.gateway.handler.predicate.RoutePredicates.path
import org.springframework.cloud.netflix.hystrix.HystrixCommands
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

@EnableDiscoveryClient
@SpringBootApplication
class EdgeServiceApplication

fun main(args: Array<String>) {

    SpringApplicationBuilder()
            .sources(EdgeServiceApplication::class.java)
            .initializers(
                beans {

                    bean {
                        val lbFilter = ref<LoadBalancerExchangeFilterFunction>()
                        WebClient.builder().filter(lbFilter).build()
                    }

                    bean {
                        gateway {
                            route {
                                id("bike-catalog-service")
                                predicate(path("/bike-catalog-service") or path("/all-bikes"))
                                uri("lb://bike-catalog-service/bikes")
                            }
                        }
                    }

                    bean {
                        router {
                            val client = ref<WebClient>()
                            GET("/trek-bikes") {
                                val otherBikes = listOf("Canyon", "Giant")

                                val trekBikes = client.get().uri("http://bike-catalog-service/bikes")
                                        .retrieve()
                                        .bodyToFlux(Bike::class.java)
                                        .filter { bike -> !otherBikes.contains(bike.name) }

                                val failureReadyBikes = HystrixCommands
                                        .from(trekBikes)
                                        .commandName("bikes")
                                        .fallback(Flux.empty())
                                        .eager()
                                        .build()

                                ServerResponse.ok().body(failureReadyBikes)
                            }
                        }
                    }

                }
            )
            .run(*args)
}

data class Bike(var id: String? = null, var name: String? = null)
