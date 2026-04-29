# Contract Testing with Pact
 
> Consumer-Driven Contract Testing — REST example  
> **Stack:** Java 21 · Spring Boot 3.4 · Gradle · Pact JVM 4.6.20 · JUnit 5 (V4 spec)
 
---
 
## 1. Service Architecture
 
Two services communicate over HTTP. `order-service` is the **Consumer** — it initiates
requests. `product-service` is the **Provider** — it responds to them.
 
```
  ┌──────────────────────┐    GET /api/products/{id}    ┌──────────────────────┐
  │    order-service     │ ──────────────────────────►  │   product-service    │
  │      (Consumer)      │ ◄──────────────────────────  │     (Provider)       │
  │      port 8080       │   200 { id, name, price }    │      port 8081       │
  └──────────┬───────────┘                              └──────────────────────┘
             │                                                     ▲
             │ generates                          verifies against │
             ▼                                                     │
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │              Pact contract file  (JSON)                                     │
  │         build/pacts/order-service-product-service.json                      │
  └─────────────────────────────────────────────────────────────────────────────┘
```
 
---
 
## 2. What Is Pact and Why Use It?
 
Pact lets each side of a service boundary be tested **in isolation** — no shared
environment, no Docker Compose, no running dependencies.
 
```
  Without Pact                          With Pact
  ────────────────────────────────      ─────────────────────────────────────
  All services must be running    →     Consumer tests run against a mock
  Slow, fragile, hard to debug    →     Provider tests replay the recorded contract
  Breaks = "which service?"       →     Breaks = "this field, this interaction"
```
 
| Aspect            | Integration test           | Contract test (Pact)       |
|-------------------|----------------------------|----------------------------|
| Services needed   | All running                | None (mock / replay)       |
| Feedback speed    | Minutes                    | Seconds                    |
| Finds             | Runtime bugs, data issues  | API contract breaks        |
| Run in CI         | Complex infra required     | Simple, no infra           |
 
Key properties of Pact contracts:
- **Matchers, not exact values** — `stringType`, `numberType`, `decimalType`. Provider can
  return any valid value of the right type.
- **Consumer defines only what it uses** — Provider can return extra fields freely.
- **JSON contract file is the source of truth** — both sides work from the same file.
---
 
## 3. How It Works — Step by Step
 
### General flow
 
```
  Consumer team                              Provider team
  ──────────────────────────────────         ──────────────────────────────────
  1. Write Pact test
     Define expected interactions
     (method, path, response body)
 
  2. Pact starts a local mock server
     Consumer's HTTP client runs
     against the mock
 
  3. Pact records interactions
     → writes contract JSON file        ──► share contract file
 
                                             4. Read contract file
                                                For each interaction:
                                                  a. call @State method (setup data)
                                                  b. replay request against real controller
                                                  c. compare response with matchers
 
                                             5. All match → contract verified
                                                Any mismatch → build fails
```
 
### In this project
 
**Consumer side** (`order-service`) — `ProductClientPactTest` defines two interactions:
 
```
  Interaction 1:  GET /api/products/1    → 200  { id: number, name: string, price: decimal }
  Interaction 2:  GET /api/products/999  → 404
```
 
`ProductClient` is pointed at the Pact mock server URL. On success, Pact writes:
 
```
  build/pacts/order-service-product-service.json
```
 
V4 Pact method signature used in this project:
 
```java
@Pact(consumer = "order-service", provider = "product-service")
V4Pact getExistingProduct(PactBuilder builder) {
    return builder
        .given("product with id 1 exists")
        .expectsToReceiveHttpInteraction("a request for product with id 1", http -> http
            .withRequest(req -> req.method("GET").path("/api/products/1"))
            .willRespondWith(res -> res.status(200)
                .body(LambdaDsl.newJsonBody(b -> b
                    .numberType("id", 1L)
                    .stringType("name", "Laptop")
                    .decimalType("price", 2999.99)
                ).build())))
        .toPact(V4Pact.class);
}
```
 
**Provider side** (`product-service`) — `ProductProviderPactTest` reads the contract and
for each interaction calls the matching `@State` method, then verifies via MockMvc:
 
```java
@State("product with id 1 exists")
void productWithId1Exists() {
    // ensure product 1 is present in the service
}
```
 
---
 
## 4. Running the Tests
 
**Step 1 — Generate the contract (Consumer)**
 
```bash
cd order-service
./gradlew test --tests "com.example.orderservice.ProductClientPactTest"
# Output: build/pacts/order-service-product-service.json
```
 
**Step 2 — Copy the contract to the Provider**
 
```bash
cp order-service/build/pacts/order-service-product-service.json \
   product-service/src/test/resources/pacts/
```
 
**Step 3 — Verify the contract (Provider)**
 
```bash
cd product-service
./gradlew test --tests "com.example.productservice.ProductProviderPactTest"
```
 
---
 
## 5. CI/CD in Production
 
In production, a **Pact Broker** replaces the manual file copy. It stores versioned
contracts and verification results. The `can-i-deploy` check queries the Broker before
any deployment.
 
### Two-team workflow
 
```
  ┌────────────────────────────────┐        ┌────────────────────────────────┐
  │  Team A — order-service        │        │  Team B — product-service      │
  │          (Consumer)            │        │          (Provider)            │
  └───────────────┬────────────────┘        └───────────────┬────────────────┘
                  │                                          │
   1. push code   │                                          │
   2. CI runs     │                                          │
      Pact tests  │                                          │
   3. publish ────┼──────► Pact Broker ◄──────────────────── ┤
      contract    │                                          │ 4. CI detects
                  │                                          │    new contract
                  │                                          │ 5. runs provider
                  │                                          │    verification
                  │                                          │ 6. publishes result
   7. can-i-      │◄──────────────────────────────────────── ┤
      deploy?     │         verification result              │
   8. deploy      │                                          │ 7. can-i-deploy
                  │                                          │ 8. deploy
```
 
### Consumer CI pipeline
 
```
1. run unit tests
2. run Pact consumer tests         → contract file generated
3. publish contract to Pact Broker
4. can-i-deploy check              → fails if provider hasn't verified yet
5. deploy to environment
```
 
### Provider CI pipeline
 
```
1. run unit tests
2. fetch contracts from Pact Broker for all consumers
3. run provider verification tests
4. publish verification results to Pact Broker
5. can-i-deploy check
6. deploy to environment
```
 
### Gradle configuration for Pact Broker
 
Consumer — publish contract:
 
```groovy
// build.gradle (order-service)
pact {
    publish {
        pactBrokerUrl = 'https://your-broker.pactflow.io'
        tags = ['main', 'production']
    }
}
```
 
```bash
./gradlew pactPublish
./gradlew canIDeploy -Ppact.broker.url=https://your-broker.pactflow.io \
                     -Ppact.provider.version=${GIT_COMMIT}
```
 
Provider — fetch contracts from Broker instead of local folder:
 
```java
// Replace @PactFolder with:
@PactBroker(
    host = "${PACT_BROKER_HOST}",
    tags = {"main"}
)
```
 
---
 
## 6. Key Concepts
 
| Concept        | Description                                                                      |
|----------------|----------------------------------------------------------------------------------|
| Consumer       | Service that calls another service's API. Defines what it expects.               |
| Provider       | Service that exposes the API. Must satisfy the consumer's expectations.           |
| Pact file      | JSON file with recorded interactions (requests + expected responses).             |
| Provider State | `@State` method — sets up required data before each interaction is verified.     |
| Matcher        | Validation rule on a field: `type`, `regex`, `decimal` — not exact values.       |
| Pact Broker    | Central server storing contracts and verification results across versions.        |
| can-i-deploy   | CLI/Gradle check: is this version safe to deploy? Queries the Broker.            |
| V4 Pact spec   | Latest contract format. Uses `PactBuilder` + `V4Pact`. Default since 4.3+.      |
