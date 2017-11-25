# BuildingCloudNativeProgressiveWebApps

* Service : bike-catalog-service
* Service : edge-service
* Server : eureka-service

## Service : bike-catalog-service

http://localhost:8080/bikes

## Service : edge-service

Load balancing :
http://localhost:8080/bikes
to
http://localhost:8081/bike-catalog-service
http://localhost:8081/all-bikes

http://localhost:8081/trek-bikes

## Server : eureka-service

See : edge-service and bike-catalog-service

http://localhost:8761/