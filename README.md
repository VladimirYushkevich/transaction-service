### Run service:
```
mvn clean package && java -jar target/tx-service-1.0-SNAPSHOT.jar
```
### Usage:
```
curl -v -X PUT --header 'Content-Type: application/json' -d '{"amount":5000,"type":"cars"}' localhost:8080/transactionservice/transaction/10 | json_pp
curl -v -X PUT --header 'Content-Type: application/json' -d '{"amount":10000,"type":"shopping","parent_id":10}' localhost:8080/transactionservice/transaction/11 | json_pp
curl -v GET localhost:8080/transactionservice/types/cars | json_pp
curl -v GET localhost:8080/transactionservice/sum/10 | json_pp
curl -v GET localhost:8080/transactionservice/sum/11 | json_pp
```

