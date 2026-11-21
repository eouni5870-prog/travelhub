# ✈️ TravelHub — Projet SOA (Service web REST en Java)

Application de gestion de destinations de voyage avec météo en temps réel.

## Objectifs du projet

| Objectif | Réalisation |
|---|---|
| 1. Développer un service web Java | API **REST** avec **Spring Boot 3** (CRUD destinations + statistiques) |
| 2. Consommer un service web gratuit | **Open-Meteo** (géocodage + météo, sans clé API) appelé depuis `WeatherService` |
| 3. Consommer son propre service (Front) | Interface HTML/CSS/JS qui appelle l'API avec `fetch()` |

## Architecture

```
Front (HTML/CSS/JS)  --fetch-->  Controller REST  -->  Service  -->  Repository (JPA)  -->  H2
                                                         |
                                                         +--RestClient-->  API Open-Meteo
```

```
src/main/java/tn/epi/travelhub/
├── model/        Destination (entité JPA), TravelStatus (enum)
├── repository/   DestinationRepository (Spring Data JPA)
├── service/      DestinationService (logique métier), WeatherService (API externe)
├── controller/   DestinationController, WeatherController (endpoints REST)
├── dto/          WeatherDto, StatsDto
├── exception/    Exceptions + GlobalExceptionHandler (erreurs JSON)
└── config/       CORS + données de départ
src/main/resources/static/   Front-end (index.html, css, js)
```

## Endpoints REST

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/destinations` | Liste (`?search=...` ou `?status=PLANNED/VISITED`) |
| GET | `/api/destinations/{id}` | Détail |
| POST | `/api/destinations` | Ajouter |
| PUT | `/api/destinations/{id}` | Modifier |
| DELETE | `/api/destinations/{id}` | Supprimer |
| GET | `/api/destinations/{id}/weather` | Météo de la destination (API externe) |
| GET | `/api/destinations/stats` | Statistiques |
| GET | `/api/weather?city=Tunis` | Météo de n'importe quelle ville |

Exemple de corps JSON (POST / PUT) :
```json
{
  "city": "Milan",
  "country": "Italie",
  "description": "Duomo et shopping",
  "budget": 1500,
  "plannedDate": "2027-07-10",
  "status": "PLANNED"
}
```

## Lancer le projet

Prérequis : **JDK 17+** et **IntelliJ IDEA** (Community suffit).

1. Ouvrir le dossier `travelhub` dans IntelliJ (il détecte le `pom.xml` et télécharge les dépendances).
2. Lancer `TravelHubApplication.java`.
3. Ouvrir **http://localhost:8080** → l'interface.
4. Console base de données : **http://localhost:8080/h2-console** (JDBC URL `jdbc:h2:file:./data/travelhub`, user `sa`).

En ligne de commande (si Maven est installé) : `mvn spring-boot:run`

Tester l'API avec Postman ou le navigateur : http://localhost:8080/api/destinations

## Technologies

Java 17 · Spring Boot 3 (Web, Data JPA, Validation) · H2 · RestClient · HTML5 / CSS3 / JavaScript (fetch)
