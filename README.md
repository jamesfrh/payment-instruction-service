REFER TO Product System Design Assignment.pdf for the tasks answers

How to Run Project
  Prerequisites
    Java 17
    Maven 3.9+

1) Build the project
2) Run the application
The service starts on: http://localhost:8080
On startup, Flyway migrations will run automatically (spring.flyway.enabled=true). You should see logs indicating the migration has been applied.

3) Access the H2 database console at http://localhost:8080/h2-console
4) Use the following settings:
JDBC URL: jdbc:h2:mem:payments
Username: sa
Password: (blank)

You should see tables such as:
PAYMENT_INSTRUCTION
PAYMENT_INSTRUCTION_EVENT

5) API Quick Test
Submit Instruction 
POST
http://localhost:8080/payment-instructions
Body (JSON):
{
  "instructionId": "INS-001",
  "sourceSystem": "CRM",
  "payerAccount": "ACC-1",
  "payeeAccount": "ACC-2",
  "amount": 100.50,
  "currency": "SGD",
  "requestedExecutionDate": "2026-02-06"
}

Expected:
201 Created for a new valid instruction
200 OK for idempotent retry (same instructionId + same payload)
409 Conflict for duplicate with different payload
400 Bad Request if payerAccount == payeeAccount (or other business rule failures)

Get Instruction Status
GET
http://localhost:8080/payment-instructions/INS-001
Expected:
200 OK if instruction exists
404 Not Found if it does not
