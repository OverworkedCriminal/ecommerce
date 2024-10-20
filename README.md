# Ecommerce

Spring Boot ecommerce REST application.

Application was meant to be a playground, not a serious commercial project

### Main features
- CRUD operations for products
- CRUD operations for product categories
- product categories support tree-like structure (categories can have parent categories)
- CRUD operations for paymentMethods
- CRUD operations for countries
- placing orders:
    - orders can be marked as completed
    - order payments can be marked as completed
    - order addresses can be updated
- JWT authentication
- PostgreSQL database (working in docker container)
- Swagger documentation

### How to run
Make sure docker is running in your system

1. Start postgres database
```
docker compose up --detach
```
2. Build maven project
```
mvn clean install
```
3. Run java project
```
java -jar ./target/ecommerce-<version>.jar
```

### Documentation
To access swagger documentation run application and visit this url:<br>
http://localhost:8080/swagger-ui/index.html
