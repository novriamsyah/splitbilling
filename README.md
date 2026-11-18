# Split Bill API 
## 1. Technology Stack

- Java 21
- Spring Boot 4.1.2
- Maven
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway Database Migration
- Swagger / OpenAPI
- Docker 

## 2. Prerequisites

Make sure the following tools are installed:

- JDK 21
- PostgreSQL database
- Git
- Docker (optional, for containerized execution)

The application uses PostgreSQL schema `tugas`. Flyway creates the schema and applies the database migrations automatically when the application starts.

## 3. Configuration

The application reads database credentials and the GitHub username from environment variables.

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `GITHUB_USERNAME` | GitHub username used for service charge calculation |

Example configuration:

```properties
DB_URL=jdbc:postgresql://YOUR_NEON_HOST/novri?sslmode=require&channel_binding=require&currentSchema=tugas
DB_USERNAME=YOUR_DATABASE_USERNAME
DB_PASSWORD=YOUR_DATABASE_PASSWORD
GITHUB_USERNAME=novriamsyah
```

Replace the database host, database name, username, and password with your own PostgreSQL credentials.

### Windows PowerShell

Set the environment variables in the terminal:

```powershell
$env:DB_URL = "jdbc:postgresql://YOUR_NEON_HOST/novri?sslmode=require&channel_binding=require&currentSchema=tugas"
$env:DB_USERNAME = "YOUR_DATABASE_USERNAME"
$env:DB_PASSWORD = "YOUR_DATABASE_PASSWORD"
$env:GITHUB_USERNAME = "novriamsyah"
```

### Linux / macOS

```bash
export DB_URL="jdbc:postgresql://YOUR_NEON_HOST/novri?sslmode=require&channel_binding=require&currentSchema=tugas"
export DB_USERNAME="YOUR_DATABASE_USERNAME"
export DB_PASSWORD="YOUR_DATABASE_PASSWORD"
export GITHUB_USERNAME="novriamsyah"
```

The `.env.example` file is provided as a configuration reference. Spring Boot does not automatically load this file; provide the variables through your shell or container environment.

## 4. Build and Run

### Build and verify


```powershell
.\mvnw.cmd clean verify
```

This command compiles the application, executes the automated tests, and verifies the Maven build.

### Run locally

After configuring the required environment variables:

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The application runs at:

```text
http://localhost:4110
```

### Run the packaged JAR

```bash
java -jar target/splitbill-0.0.1-SNAPSHOT.jar
```

Make sure the required environment variables are available to the process.

### API documentation

Once the application is running:

- Swagger UI: http://localhost:4110/swagger-ui.html

## 5. Docker

The project includes a multi-stage Dockerfile based on Java 21.

- Build stage: Eclipse Temurin JDK 21 Alpine.
- Runtime stage: Eclipse Temurin JRE 21 Alpine.
- Application port: `4110`.

### Build Docker image

```bash
docker build -t splitbilling:latest .
```

### Run Docker container

Linux / macOS:

```bash
docker run --rm \
  -p 4110:4110 \
  -e DB_URL="$DB_URL" \
  -e DB_USERNAME="$DB_USERNAME" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e GITHUB_USERNAME="$GITHUB_USERNAME" \
  splitbilling:latest
```

Windows PowerShell:

```powershell
docker run --rm -p 4110:4110 `
  -e "DB_URL=$env:DB_URL" `
  -e "DB_USERNAME=$env:DB_USERNAME" `
  -e "DB_PASSWORD=$env:DB_PASSWORD" `
  -e "GITHUB_USERNAME=$env:GITHUB_USERNAME" `
  splitbilling:latest
```

The PostgreSQL database must be reachable from the Docker container. For a hosted database such as Neon, use its accessible connection URL.

## 6. API Endpoints

Base URL:

```text
http://localhost:4110
```

The API uses UUIDs to identify groups, participants, and expenses. Use the IDs returned by previous requests when calling subsequent endpoints.

For the following examples, set these variables in a Bash-compatible terminal:

```bash
BASE_URL=http://localhost:4110

GROUP_ID=<group-uuid>
PARTICIPANT_ID=<participant-uuid>
EXPENSE_ID=<expense-uuid>
```

### 6.1 Group Management

#### 1. Create Group

`POST /api/groups`

Creates a new bill group.

```bash
curl -X POST "$BASE_URL/api/groups" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Makan Bersama",
    "currencyCode": "IDR"
  }'
```

`currencyCode` is optional. If omitted or blank, the application defaults to `IDR`.

#### 2. Get All Groups

`GET /api/groups`

Retrieves all groups ordered by creation time, newest first.

```bash
curl -X GET "$BASE_URL/api/groups"
```

#### 3. Get Group by ID

`GET /api/groups/{groupId}`

Retrieves a group by its UUID.

```bash
curl -X GET "$BASE_URL/api/groups/$GROUP_ID"
```

### 6.2 Participant Management

#### 4. Create Participant

`POST /api/groups/{groupId}/participants`

Adds a participant to an existing group.

```bash
curl -X POST "$BASE_URL/api/groups/$GROUP_ID/participants" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Novri"
  }'
```

Repeat this request to add other participants.

#### 5. Get All Participants

`GET /api/groups/{groupId}/participants`

Retrieves all participants belonging to a group.

```bash
curl -X GET "$BASE_URL/api/groups/$GROUP_ID/participants"
```

#### 6. Get Participant by ID

`GET /api/groups/{groupId}/participants/{participantId}`

```bash
curl -X GET "$BASE_URL/api/groups/$GROUP_ID/participants/$PARTICIPANT_ID"
```

#### 7. Update Participant

`PUT /api/groups/{groupId}/participants/{participantId}`

Updates a participant's name.

```bash
curl -X PUT "$BASE_URL/api/groups/$GROUP_ID/participants/$PARTICIPANT_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Novri Amsyah"
  }'
```

#### 8. Delete Participant

`DELETE /api/groups/{groupId}/participants/{participantId}`

```bash
curl -X DELETE "$BASE_URL/api/groups/$GROUP_ID/participants/$PARTICIPANT_ID"
```

The endpoint returns HTTP `204 No Content` when deletion succeeds. A participant referenced by an expense or expense share cannot be deleted.

### 6.3 Expense Management

#### 9. Create Expense

`POST /api/groups/{groupId}/expenses`

Records a new expense and the participant who paid for it.

```bash
curl -X POST "$BASE_URL/api/groups/$GROUP_ID/expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "payerId": "<payer-participant-uuid>",
    "description": "Makan Siang",
    "amount": 150000.00,
    "splitType": "EQUAL",
    "expenseDate": "2026-11-17"
  }'
```

Request fields:

| Field | Description |
|---|---|
| `payerId` | UUID of the participant who paid |
| `description` | Expense description |
| `amount` | Total expense amount |
| `splitType` | `EQUAL`, `EXACT`, or `PERCENTAGE` |
| `expenseDate` | Expense date |

All monetary values use `BigDecimal`.

Creating an expense does not automatically allocate the expense among participants. Use the expense shares endpoint to create its allocations.

#### 10. Get All Expenses

`GET /api/groups/{groupId}/expenses`

Retrieves all expenses belonging to a group.

```bash
curl -X GET "$BASE_URL/api/groups/$GROUP_ID/expenses"
```

#### 11. Get Expense by ID

`GET /api/groups/{groupId}/expenses/{expenseId}`

```bash
curl -X GET "$BASE_URL/api/groups/$GROUP_ID/expenses/$EXPENSE_ID"
```

#### 12. Update Expense

`PUT /api/groups/{groupId}/expenses/{expenseId}`

Updates an existing expense.

```bash
curl -X PUT "$BASE_URL/api/groups/$GROUP_ID/expenses/$EXPENSE_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "payerId": "<payer-participant-uuid>",
    "description": "Makan Siang Updated",
    "amount": 150000.00,
    "splitType": "EQUAL",
    "expenseDate": "2026-11-17T12:00:00+07:00"
  }'
```

If the expense amount or split type changes, the existing expense shares are deleted because their previous allocations are no longer valid. New allocations must be created before requesting settlement.

Changes to the payer, description, or expense date do not automatically invalidate the allocation amounts.

#### 13. Delete Expense

`DELETE /api/groups/{groupId}/expenses/{expenseId}`

```bash
curl -X DELETE "$BASE_URL/api/groups/$GROUP_ID/expenses/$EXPENSE_ID"
```

The endpoint returns HTTP `204 No Content` when deletion succeeds. Associated expense shares are deleted through the database foreign key cascade.

### 6.4 Expense Share Management

Expense shares define how an expense is allocated among group participants.

The API supports three strategies:

- `EQUAL`: divides the expense equally among selected participants.
- `EXACT`: assigns an explicit monetary amount to each participant.
- `PERCENTAGE`: assigns a percentage of the expense to each participant.

The endpoint replaces the previous allocations for the specified expense.

#### 14. Replace Expense Shares — EQUAL

`PUT /api/groups/{groupId}/expenses/{expenseId}/shares`

For an expense with `splitType` set to `EQUAL`, provide the participant IDs.

```bash
curl -X PUT "$BASE_URL/api/groups/$GROUP_ID/expenses/$EXPENSE_ID/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "allocations": [
      {
        "participantId": "<participant-1-uuid>"
      },
      {
        "participantId": "<participant-2-uuid>"
      },
      {
        "participantId": "<participant-3-uuid>"
      }
    ]
  }'
```

The application calculates each participant's share automatically and distributes any remaining cents so that the allocation total equals the expense amount.

#### Replace Expense Shares — EXACT

For an expense with `splitType` set to `EXACT`, provide the amount allocated to each participant.

```bash
curl -X PUT "$BASE_URL/api/groups/$GROUP_ID/expenses/$EXPENSE_ID/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "allocations": [
      {
        "participantId": "<participant-1-uuid>",
        "shareAmount": 70000.00
      },
      {
        "participantId": "<participant-2-uuid>",
        "shareAmount": 50000.00
      },
      {
        "participantId": "<participant-3-uuid>",
        "shareAmount": 30000.00
      }
    ]
  }'
```

The sum of all `shareAmount` values must equal the expense amount.

#### Replace Expense Shares — PERCENTAGE

For an expense with `splitType` set to `PERCENTAGE`, provide the percentage allocated to each participant.

```bash
curl -X PUT "$BASE_URL/api/groups/$GROUP_ID/expenses/$EXPENSE_ID/shares" \
  -H "Content-Type: application/json" \
  -d '{
    "allocations": [
      {
        "participantId": "<participant-1-uuid>",
        "sharePercentage": 50.0000
      },
      {
        "participantId": "<participant-2-uuid>",
        "sharePercentage": 30.0000
      },
      {
        "participantId": "<participant-3-uuid>",
        "sharePercentage": 20.0000
      }
    ]
  }'
```

The sum of all `sharePercentage` values must equal exactly `100`.

The application calculates the monetary allocations and distributes remaining cents according to the largest fractional remainders.

#### 15. Get Expense Shares

`GET /api/groups/{groupId}/expenses/{expenseId}/shares`

Retrieves all allocations for a specific expense.

```bash
curl -X GET "$BASE_URL/api/groups/$GROUP_ID/expenses/$EXPENSE_ID/shares"
```

### 6.5 Settlement

#### 16. Calculate Group Settlement

`GET /api/groups/{groupId}/settlement`

Calculates participant balances and generates suggested transfers to settle the group's expenses.

```bash
curl -X GET "$BASE_URL/api/groups/$GROUP_ID/settlement"
```

The settlement response includes:

- Total group expenses.
- Total amount paid.
- Total allocated amount.
- Service charge percentage and amount.
- Each participant's total paid amount.
- Each participant's allocated share.
- Each participant's net balance and status.
- Suggested transfers between debtors and creditors.

An expense must have valid shares whose total equals its expense amount. If an expense is missing allocations or its allocations are inconsistent, the endpoint returns HTTP `409 Conflict`.

The settlement endpoint calculates suggested transfers only. It does not execute payments or record payment completion.

## 7. Settlement Calculation

The net balance for each participant is calculated as:

```text
netBalance = totalPaid - totalShare
```

Participant status:

| Status | Meaning |
|---|---|
| `CREDITOR` | The participant paid more than their allocated share. |
| `DEBTOR` | The participant's allocated share is greater than what they paid. |
| `SETTLED` | The participant's paid amount equals their allocated share. |

The application generates transfers by matching debtors with creditors. Each transfer amount is the smaller of the remaining debtor balance and creditor balance.

This greedy matching strategy produces a valid settlement plan, but it does not guarantee the globally minimum number of transfers for every possible balance distribution.

## 8. Personalization — Service Charge

**GitHub Username:** `novriamsyah`

The service charge percentage is calculated from the lowercase GitHub username as required by the challenge.

Calculation:

```text
GitHub username: novriamsyah

Unicode / ASCII values:
n = 110
o = 111
v = 118
r = 114
i = 105
a = 97
m = 109
s = 115
y = 121
a = 97
h = 104

Total Unicode sum = 1201

service_charge_pct = 1201 % 10
service_charge_pct = 1%
```

The service charge amount is calculated from the total group expenses:

```text
service_charge_amount =
    total group expenses * service_charge_pct / 100
```

Example:

```text
Total group expenses = Rp240,000.00
Service charge       = 1%

service_charge_amount = 240000.00 * 1 / 100
service_charge_amount = Rp2,400.00
```

Every settlement response includes these two fields:

```json
{
  "service_charge_pct": 1,
  "service_charge_amount": 2400.00
}
```

Both values are calculated dynamically in application code using the configured GitHub username. They are not hardcoded.

The service charge is returned as a separate informational amount. It is not added to participant shares or settlement transfer balances.

## 9. Tests

Run the automated test suite:

Windows:

```powershell
.\mvnw.cmd test
```

Linux / macOS:

```bash
./mvnw test
```

The project includes unit tests for:

- Service charge percentage calculation from the GitHub username.
- Service charge monetary calculation.
- Equal expense allocation and remaining-cent distribution.
- Exact expense allocation validation.
- Percentage expense allocation and rounding.
- Settlement balance and transfer calculation.
- Settlement rejection when expense shares are incomplete.
- Settlement when participants have no outstanding balance.

## 10. Design Decision

The hardest design decision was separating an expense from the allocation of that expense among participants. I chose separate `expenses` and `expense_shares` records so that one expense can support equal, exact-amount, and percentage-based splitting without overloading the expense model. The trade-off is that creating an expense and allocating it require two API calls, and settlement must reject expenses whose allocations are missing or inconsistent. This keeps the data model explicit and prevents the API from returning misleading balances when an expense has not been fully allocated.