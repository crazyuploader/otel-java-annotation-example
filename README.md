# OTel Java Annotation Example

## Usage

### Prepare SQLite Database

Run `sqlite3 db.sqlite3` to open database, and paste the SQL statement to populate the database -

```sql
CREATE TABLE items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL
);

INSERT INTO items(name) VALUES ('apple'), ('banana'), ('mango'), ('grapes');

-- Verify the data
SELECT * FROM items;
```

### Download OTel Java binary agent

```bash
curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
```

### Build

```bash
mvn clean compile
mvn clean package
```

### Run

```bash
OTEL_LOG_LEVEL=debug \
OTEL_RESOURCE_ATTRIBUTES=service.name=my-java-api \
OTEL_EXPORTER_OTLP_ENDPOINT=https://ingest.in.signoz.cloud:443 \
OTEL_EXPORTER_OTLP_HEADERS=signoz-ingestion-key=api-key \
java -javaagent:$PWD/opentelemetry-javaagent.jar -jar target/my-java-api-1.0-SNAPSHOT.jar
```
