# ordering-system

This is the main repository to run the application.

This service depends on 2 other dependencies:

common: https://github.com/trung2701/common/blob/main/README.md

connectors: https://github.com/trung2701/connectors/blob/main/README.md

After finish building these 2 services, a docker compose file is already created. Get it up and running using command:

`docker-compose up`

Then, we can test the application using postman.

Note: a postman collection is commited within the source code!
Also noted that due to integration testing purpose, a user is already created and hard coded
Customer id in Endpoint create order should not be modified 

When opening the source code, it should be compiled to avoid some error detected by code editor using command:

```java
mvn clean install
```

or

```java
mvn clean install -Dmaven.test.skip=true
```