# FinPay-Intelligence-Platform
Cloud-native payment processing platform with GenAI fraud intelligence — Spring Boot microservices on ECS/EKS, AWS Bedrock RAG, full IaC via Terraform.


# FinPay Intelligence — Complete Project Structure

```
finpay-intelligence/
│
├── pom.xml                                          ← ROOT PARENT POM
│
├── docker-compose.yml                               ← local full stack (all services)
├── docker-compose-infra.yml                         ← infra only (postgres, kafka, redis etc)
│
├── .github/
│   └── workflows/
│       ├── ci.yml                                   ← build + test + testcontainers
│       └── deploy.yml                               ← docker push ECR + helm deploy
│
│
├── services/
│   │
│   │
│   ├── order-service/                               ══ EKS (Helm) ══
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   │
│   │   ├── helm/
│   │   │   ├── Chart.yaml
│   │   │   ├── values.yaml
│   │   │   └── templates/
│   │   │       ├── deployment.yaml
│   │   │       ├── service.yaml
│   │   │       ├── ingress.yaml
│   │   │       └── serviceaccount.yaml              ← IRSA annotation here
│   │   │
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/finpay/order/
│   │       │   │   │
│   │       │   │   ├── OrderServiceApplication.java           ← MAIN CLASS
│   │       │   │   │
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java                ← JWT resource server config
│   │       │   │   │   ├── JpaConfig.java                     ← JPA auditing, entity scan
│   │       │   │   │   ├── FeignConfig.java                   ← Feign client config
│   │       │   │   │   └── OpenApiConfig.java                 ← Swagger UI config
│   │       │   │   │
│   │       │   │   ├── controller/
│   │       │   │   │   └── OrderController.java               ← REST endpoints + HATEOAS
│   │       │   │   │
│   │       │   │   ├── service/
│   │       │   │   │   ├── OrderService.java                  ← CRUD + business logic
│   │       │   │   │   ├── OrderSagaService.java              ← Saga orchestration
│   │       │   │   │   └── AuditLogService.java               ← REQUIRES_NEW transaction
│   │       │   │   │
│   │       │   │   ├── repository/
│   │       │   │   │   ├── OrderRepository.java               ← JPA repo + custom queries
│   │       │   │   │   ├── AuditLogRepository.java
│   │       │   │   │   └── OrderSpecification.java            ← dynamic filter specs
│   │       │   │   │
│   │       │   │   ├── domain/
│   │       │   │   │   ├── entity/
│   │       │   │   │   │   ├── Order.java                     ← @Version optimistic lock
│   │       │   │   │   │   └── AuditLog.java                  ← audit trail entity
│   │       │   │   │   ├── enums/
│   │       │   │   │   │   └── OrderStatus.java               ← CREATED/PENDING/CONFIRMED/FAILED
│   │       │   │   │   └── projection/
│   │       │   │   │       └── OrderSummary.java              ← lightweight list projection
│   │       │   │   │
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   └── CreateOrderRequest.java        ← @Valid + @IBAN + @ValidCurrency
│   │       │   │   │   └── response/
│   │       │   │   │       └── OrderResponse.java             ← HATEOAS RepresentationModel
│   │       │   │   │
│   │       │   │   ├── client/
│   │       │   │   │   ├── PaymentClient.java                 ← Feign → payment-service
│   │       │   │   │   └── FraudClient.java                   ← Feign → fraud-rag-service
│   │       │   │   │
│   │       │   │   ├── validation/
│   │       │   │   │   ├── IbanValidator.java                 ← custom @IBAN constraint
│   │       │   │   │   ├── ValidIban.java                     ← @interface annotation
│   │       │   │   │   ├── CurrencyValidator.java             ← custom @ValidCurrency
│   │       │   │   │   └── ValidCurrency.java                 ← @interface annotation
│   │       │   │   │
│   │       │   │   ├── exception/
│   │       │   │   │   ├── GlobalExceptionHandler.java        ← @RestControllerAdvice
│   │       │   │   │   ├── OrderNotFoundException.java
│   │       │   │   │   └── SagaRollbackException.java
│   │       │   │   │
│   │       │   │   └── mapper/
│   │       │   │       └── OrderMapper.java                   ← MapStruct entity<->dto
│   │       │   │
│   │       │   └── resources/
│   │       │       ├── application.yml                        ← base config
│   │       │       ├── application-local.yml                  ← local docker-compose config
│   │       │       ├── application-ci.yml                     ← testcontainers config
│   │       │       └── db/migration/
│   │       │           ├── V1__create_orders_table.sql
│   │       │           └── V2__create_audit_log_table.sql
│   │       │
│   │       └── test/
│   │           └── java/com/finpay/order/
│   │               ├── unit/
│   │               │   ├── OrderServiceTest.java              ← pure Mockito, no Spring
│   │               │   ├── OrderSagaServiceTest.java          ← saga rollback scenarios
│   │               │   └── IbanValidatorTest.java             ← validator unit test
│   │               ├── integration/
│   │               │   ├── OrderControllerIT.java             ← @SpringBootTest + Testcontainers
│   │               │   └── OrderRepositoryIT.java             ← JPA queries + specs test
│   │               ├── contract/
│   │               │   └── OrderPaymentContractTest.java      ← Pact consumer contract
│   │               └── config/
│   │                   └── TestContainersConfig.java          ← shared container config
│   │
│   │
│   ├── payment-service/                             ══ ECS Fargate ══
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   │
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/finpay/payment/
│   │       │   │   │
│   │       │   │   ├── PaymentServiceApplication.java         ← MAIN CLASS
│   │       │   │   │
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java                ← JWT resource server
│   │       │   │   │   ├── RedissonConfig.java                ← distributed lock setup
│   │       │   │   │   ├── KmsConfig.java                     ← AWS KMS client bean
│   │       │   │   │   └── AsyncConfig.java                   ← CompletableFuture thread pool
│   │       │   │   │
│   │       │   │   ├── controller/
│   │       │   │   │   ├── PaymentController.java             ← REST: debit, refund, status
│   │       │   │   │   └── RefundController.java
│   │       │   │   │
│   │       │   │   ├── service/
│   │       │   │   │   ├── PaymentService.java                ← orchestrates payment flow
│   │       │   │   │   ├── WalletService.java                 ← pessimistic lock on debit
│   │       │   │   │   ├── IdempotencyService.java            ← Redisson + DynamoDB check
│   │       │   │   │   ├── RefundService.java
│   │       │   │   │   └── KmsEncryptionService.java          ← envelope encrypt card tokens
│   │       │   │   │
│   │       │   │   ├── repository/
│   │       │   │   │   ├── PaymentRepository.java
│   │       │   │   │   ├── WalletRepository.java              ← @Lock(PESSIMISTIC_WRITE)
│   │       │   │   │   └── IdempotencyRepository.java         ← DynamoDB TTL store
│   │       │   │   │
│   │       │   │   ├── domain/
│   │       │   │   │   ├── entity/
│   │       │   │   │   │   ├── Payment.java
│   │       │   │   │   │   └── Wallet.java
│   │       │   │   │   └── enums/
│   │       │   │   │       └── PaymentStatus.java
│   │       │   │   │
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   ├── PaymentRequest.java
│   │       │   │   │   │   └── RefundRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── PaymentResponse.java
│   │       │   │   │       └── RefundResponse.java
│   │       │   │   │
│   │       │   │   ├── provider/
│   │       │   │   │   ├── PaymentProvider.java               ← interface
│   │       │   │   │   ├── RazorpayProvider.java              ← CompletableFuture parallel call
│   │       │   │   │   └── StripeProvider.java                ← CompletableFuture parallel call
│   │       │   │   │
│   │       │   │   ├── exception/
│   │       │   │   │   ├── GlobalExceptionHandler.java
│   │       │   │   │   ├── InsufficientFundsException.java
│   │       │   │   │   └── FraudDetectedException.java
│   │       │   │   │
│   │       │   │   └── mapper/
│   │       │   │       └── PaymentMapper.java
│   │       │   │
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       ├── application-local.yml
│   │       │       └── application-ci.yml
│   │       │
│   │       └── test/
│   │           └── java/com/finpay/payment/
│   │               ├── unit/
│   │               │   ├── PaymentServiceTest.java
│   │               │   ├── WalletServiceTest.java
│   │               │   └── IdempotencyServiceTest.java
│   │               ├── integration/
│   │               │   └── PaymentControllerIT.java
│   │               └── config/
│   │                   └── TestContainersConfig.java
│   │
│   │
│   ├── ledger-service/                              ══ EKS (Helm) ══
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   │
│   │   ├── helm/
│   │   │   ├── Chart.yaml
│   │   │   ├── values.yaml
│   │   │   └── templates/
│   │   │       ├── deployment.yaml
│   │   │       ├── service.yaml
│   │   │       └── serviceaccount.yaml
│   │   │
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/finpay/ledger/
│   │       │   │   │
│   │       │   │   ├── LedgerServiceApplication.java          ← MAIN CLASS
│   │       │   │   │
│   │       │   │   ├── config/
│   │       │   │   │   ├── DynamoDbConfig.java                ← DynamoDB client bean
│   │       │   │   │   └── KafkaConfig.java                   ← consumer group topology
│   │       │   │   │
│   │       │   │   ├── controller/
│   │       │   │   │   └── LedgerController.java
│   │       │   │   │
│   │       │   │   ├── service/
│   │       │   │   │   ├── LedgerService.java                 ← double-entry recording
│   │       │   │   │   └── AuditEventService.java             ← DynamoDB audit writes
│   │       │   │   │
│   │       │   │   ├── repository/
│   │       │   │   │   ├── LedgerRepository.java              ← DynamoDB enhanced client
│   │       │   │   │   └── AuditEventRepository.java
│   │       │   │   │
│   │       │   │   ├── domain/
│   │       │   │   │   ├── LedgerEntry.java                   ← @DynamoDbBean
│   │       │   │   │   └── AuditEvent.java                    ← TTL field
│   │       │   │   │
│   │       │   │   ├── dto/
│   │       │   │   │   └── response/
│   │       │   │   │       └── LedgerResponse.java
│   │       │   │   │
│   │       │   │   ├── kafka/
│   │       │   │   │   ├── LedgerEventConsumer.java           ← @KafkaListener
│   │       │   │   │   └── LedgerEventProducer.java           ← KafkaTemplate
│   │       │   │   │
│   │       │   │   └── exception/
│   │       │   │       └── GlobalExceptionHandler.java
│   │       │   │
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       ├── application-local.yml
│   │       │       └── application-ci.yml
│   │       │
│   │       └── test/
│   │           └── java/com/finpay/ledger/
│   │               ├── unit/
│   │               │   └── LedgerServiceTest.java
│   │               └── integration/
│   │                   └── LedgerControllerIT.java
│   │
│   │
│   ├── fraud-rag-service/                           ══ ECS ══
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   │
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/finpay/fraud/
│   │       │   │   │
│   │       │   │   ├── FraudRagServiceApplication.java        ← MAIN CLASS
│   │       │   │   │
│   │       │   │   ├── config/
│   │       │   │   │   ├── BedrockConfig.java                 ← Claude + Titan client
│   │       │   │   │   ├── ChromaConfig.java                  ← vector store bean
│   │       │   │   │   └── MongoConfig.java                   ← audit log store
│   │       │   │   │
│   │       │   │   ├── controller/
│   │       │   │   │   ├── FraudScoreController.java          ← POST /api/fraud/score
│   │       │   │   │   └── PolicyQueryController.java         ← POST /api/fraud/query (ADMIN)
│   │       │   │   │
│   │       │   │   ├── service/
│   │       │   │   │   ├── RagQueryService.java               ← embed+search+prompt+LLM
│   │       │   │   │   ├── DocumentIngestionService.java      ← S3→chunk→embed→ChromaDB
│   │       │   │   │   ├── FraudScoringService.java           ← score transaction via LLM
│   │       │   │   │   └── EmbeddingService.java              ← Bedrock Titan embeddings
│   │       │   │   │
│   │       │   │   ├── repository/
│   │       │   │   │   └── QueryAuditRepository.java          ← MongoDB
│   │       │   │   │
│   │       │   │   ├── domain/
│   │       │   │   │   └── QueryAuditLog.java                 ← @Document (MongoDB)
│   │       │   │   │
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   ├── FraudScoreRequest.java
│   │       │   │   │   │   └── PolicyQueryRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── FraudScoreResponse.java
│   │       │   │   │       └── PolicyQueryResponse.java
│   │       │   │   │
│   │       │   │   └── exception/
│   │       │   │       └── GlobalExceptionHandler.java
│   │       │   │
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       ├── application-local.yml
│   │       │       └── fraud-policies/
│   │       │           └── sample-fraud-policy.pdf            ← seed document for RAG
│   │       │
│   │       └── test/
│   │           └── java/com/finpay/fraud/
│   │               ├── unit/
│   │               │   ├── RagQueryServiceTest.java
│   │               │   └── FraudScoringServiceTest.java
│   │               └── integration/
│   │                   └── FraudScoreControllerIT.java
│   │
│   │
│   └── auth-service/                                ══ ECS ══
│       ├── pom.xml
│       ├── Dockerfile
│       │
│       └── src/
│           ├── main/
│           │   ├── java/com/finpay/auth/
│           │   │   │
│           │   │   ├── AuthServiceApplication.java            ← MAIN CLASS
│           │   │   │
│           │   │   ├── config/
│           │   │   │   ├── AuthServerConfig.java              ← Spring Authorization Server
│           │   │   │   └── SecurityConfig.java                ← role: USER/MERCHANT/ADMIN
│           │   │   │
│           │   │   ├── controller/
│           │   │   │   └── AuthController.java                ← /token, /introspect
│           │   │   │
│           │   │   ├── service/
│           │   │   │   └── UserDetailsServiceImpl.java        ← load user + roles from DB
│           │   │   │
│           │   │   └── domain/
│           │   │       ├── entity/
│           │   │       │   ├── User.java
│           │   │       │   └── Role.java
│           │   │       └── enums/
│           │   │           └── RoleType.java                  ← USER, MERCHANT, ADMIN
│           │   │
│           │   └── resources/
│           │       ├── application.yml
│           │       └── application-local.yml
│           │
│           └── test/
│               └── java/com/finpay/auth/
│                   └── integration/
│                       └── AuthFlowIT.java                    ← token issue + validate flow
│
│
├── lambdas/
│   │
│   ├── notification-lambda/                         ══ SQS trigger ══
│   │   ├── pom.xml
│   │   └── src/main/java/com/finpay/lambda/notification/
│   │       ├── NotificationHandler.java             ← MAIN CLASS (implements RequestHandler)
│   │       ├── service/
│   │       │   └── EmailService.java                ← SES send
│   │       └── model/
│   │           └── PaymentEvent.java
│   │
│   └── invoice-lambda/                              ══ S3 event trigger ══
│       ├── pom.xml
│       └── src/main/java/com/finpay/lambda/invoice/
│           ├── InvoiceHandler.java                  ← MAIN CLASS (implements RequestHandler)
│           ├── service/
│           │   └── PdfGeneratorService.java         ← generate + upload PDF to S3
│           └── model/
│               └── InvoiceRequest.java
│
│
└── terraform/
    ├── main.tf                                      ← root, calls all modules
    ├── variables.tf
    ├── outputs.tf
    │
    ├── modules/
    │   ├── vpc/
    │   │   ├── main.tf                              ← public/private/isolated subnets
    │   │   ├── variables.tf
    │   │   └── outputs.tf
    │   ├── rds-aurora/
    │   │   ├── main.tf                              ← Aurora PostgreSQL + read replica
    │   │   ├── variables.tf
    │   │   └── outputs.tf
    │   ├── ecs-fargate/
    │   │   ├── main.tf                              ← payment-service + fraud-rag-service
    │   │   ├── variables.tf
    │   │   └── outputs.tf
    │   ├── eks-cluster/
    │   │   ├── main.tf                              ← order-service + ledger-service
    │   │   ├── variables.tf
    │   │   └── outputs.tf
    │   ├── dynamodb/
    │   │   ├── main.tf                              ← idempotency table (TTL) + audit table
    │   │   └── variables.tf
    │   ├── elasticache/
    │   │   ├── main.tf                              ← Redis cluster
    │   │   └── variables.tf
    │   ├── kms/
    │   │   ├── main.tf                              ← key for card token encryption
    │   │   └── outputs.tf
    │   ├── lambda/
    │   │   ├── main.tf                              ← notification + invoice lambdas
    │   │   └── variables.tf
    │   └── iam-irsa/
    │       ├── main.tf                              ← pod-level IAM roles (IRSA)
    │       └── variables.tf
    │
    └── environments/
        ├── staging/
        │   ├── main.tf
        │   └── terraform.tfvars
        └── prod/
            ├── main.tf
            └── terraform.tfvars
```