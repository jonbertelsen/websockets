# WebSocket terning-demo

Et lille Java-projekt bygget med Javalin, som demonstrerer realtidskommunikation med WebSockets. Applikationen serverer en simpel HTML-klient og lader alle forbundne brugere se, når en terning bliver rullet.

## Formål

Projektet viser:

- hvordan man starter en Javalin-server
- hvordan man serverer statiske filer fra `src/main/resources/public`
- hvordan man opretter en WebSocket-endpoint
- hvordan man broadcaster beskeder til alle forbundne klienter

## Teknologier

- Java 25
- Maven
- Javalin 7.0.1
- SLF4J Simple 2.0.17

## Projektstruktur

```text
src/
  main/
    java/app/Main.java                 Server og WebSocket-logik
    resources/public/index.html       Simpel browserklient
pom.xml                               Maven-konfiguration
```

## Funktionalitet

Applikationen består af to dele:

1. En HTTP-server på port `7070`
2. En WebSocket-forbindelse på `/ws/dice`

Når en klient forbinder:

- gemmes klienten i en trådsikker samling
- antallet af aktive klienter broadcastes til alle

Når en klient sender teksten `roll`:

- serveren genererer et tilfældigt tal mellem 1 og 6
- resultatet sendes til alle forbundne klienter som JSON
- alle klienter opdaterer terningens visning og historik

Når en klient lukker forbindelsen:

- klienten fjernes fra samlingen
- opdateret antal spillere broadcastes til alle

## Endpoints

### `GET /api/health`

Returnerer teksten `OK` og kan bruges som simpelt health check.

### `GET /`

Serverer den statiske fil `index.html`, som fungerer som klient til WebSocket-demoen.

### `WS /ws/dice`

WebSocket-endpoint til realtidskommunikation.

Klienten sender:

```text
roll
```

Serveren sender to typer JSON-beskeder.

Spillerantal:

```json
{
  "type": "players",
  "count": 3
}
```

Terningekast:

```json
{
  "type": "roll",
  "value": 5,
  "playerCount": 3
}
```

## Sådan køres projektet

### Krav

- Java 25 installeret
- Maven installeret

### Start applikationen

Byg projektet:

```bash
mvn package
```

Start serveren:

```bash
mvn exec:java -Dexec.mainClass=app.Main
```

Alternativt kan du køre `app.Main` direkte fra din IDE.

Åbn derefter i browseren:

```text
http://localhost:7070
```

## Hvordan klienten virker

Filen `src/main/resources/public/index.html`:

- opretter automatisk en WebSocket-forbindelse til `/ws/dice`
- viser forbindelsesstatus
- viser hvor mange klienter der er forbundet
- sender `roll`, når brugeren klikker på knappen
- viser seneste terningekast som Unicode-terning
- tilføjer hvert kast til en historikliste

Klienten vælger automatisk `ws://` eller `wss://` afhængigt af om siden er åbnet via HTTP eller HTTPS.

## Vigtig kode

I [`src/main/java/app/Main.java`](/Users/jobe/Documents/dat3/dat3Spring2026/api-test/websockets/src/main/java/app/Main.java) sker det centrale:

- `ConcurrentHashMap.newKeySet()` bruges til at holde styr på aktive klienter på en trådsikker måde
- `ws.onConnect`, `ws.onClose` og `ws.onMessage` håndterer WebSocket-livscyklussen
- `broadcast(...)` sender beskeder til alle åbne forbindelser
- `broadcastPlayerCount()` sender opdateret antal spillere

## Mulige forbedringer

- tilføje fejlhåndtering i klienten, hvis WebSocket ikke kan forbindes
- vise hvilken bruger der rullede terningen
- gemme historik på serveren
- tilføje tests
- konfigurere port via miljøvariabel eller system property
