# expense-service (Persistence Solution)

Este proyecto es la **solución completa** del laboratorio de persistencia con Spring Boot 4.

## Tecnologías

- Spring Boot 4.0.2
- Spring Data JPA
- H2 Database (en memoria)
- Bean Validation
- springdoc-openapi (Swagger UI)

## Ejecutar

```bash
cd expense-service
mvn spring-boot:run
```

## Endpoints

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **H2 Console**: http://localhost:8080/h2-console
- **API REST**: http://localhost:8080/expenses

## Estructura

```
src/main/java/com/bcp/training/
├── ExpenseServiceApplication.java
├── model/
│   ├── Associate.java
│   └── Expense.java
├── repository/
│   ├── AssociateRepository.java
│   └── ExpenseRepository.java
├── service/
│   └── ExpenseService.java
└── controller/
    └── ExpenseController.java
```
