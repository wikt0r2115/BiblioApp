# BiblioApp

REST API do zarządzania książkami, czytelnikami i wypożyczeniami. Projekt używa Java 25, Spring Boot, JPA, Flyway oraz H2 lub PostgreSQL.

## Uruchomienie z H2

Wymagany jest JDK 25. Projekt zawiera Maven Wrapper, więc nie trzeba instalować Mavena.

```bash
./mvnw spring-boot:run
```

Profil `h2` jest domyślny. Baza działa w pamięci i znika po zatrzymaniu aplikacji. API jest dostępne pod `http://localhost:8080`.

## Uruchomienie z PostgreSQL

Wymagany jest Docker z wtyczką Compose. Jeśli nie masz jeszcze pliku `.env`, skopiuj `.env.example` do `.env` i ustaw własne hasło. Plik `.env` jest ignorowany przez Git.

```bash
cp -n .env.example .env
sudo docker compose up -d --wait
set -a
. ./.env
set +a
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

Opcja `-n` chroni istniejący plik `.env` przed nadpisaniem. `sudo` jest potrzebne, gdy użytkownik nie ma dostępu do gniazda Docker; jeśli masz taki dostęp, uruchom `docker compose` bez `sudo`.

PostgreSQL nasłuchuje lokalnie na porcie 5432. Flyway automatycznie uruchamia migrację schematu, a Hibernate sprawdza jego zgodność z encjami. Dane są przechowywane w wolumenie `postgres_data`. `sudo docker compose down` zatrzymuje środowisko i zachowuje dane; dodanie `--volumes` usuwa również bazę.

## Najważniejsze endpointy

| Zasób | Operacje |
| --- | --- |
| Autorzy | `POST /author`, `GET /author` |
| Kategorie | `POST /category`, `GET /category` |
| Książki | `POST /book`, `GET /book`, `GET /book/{id}`, `PUT /book/{id}`, `PUT /book/{id}/isbn`, `DELETE /book/{id}` |
| Czytelnicy | `POST /member`, `GET /member/{id}`, `PUT /member/{id}`, `DELETE /member/{id}` |
| Wypożyczenia | `POST /loans`, `GET /loans/{id}`, `POST /loans/{id}/return`, `GET /members/{id}/loans/active`, `GET /members/{id}/loans/history` |

`GET /book` obsługuje parametry `title`, `authorId`, `available`, `page`, `size` i `sort`, np. `/book?title=java&available=true&page=0&size=10`.

Przykładowy przepływ (identyfikatory `1` zastąp wartościami otrzymanymi w odpowiedziach):

```bash
curl -i -X POST http://localhost:8080/author -H 'Content-Type: application/json' -d '{"firstName":"Jan","lastName":"Kowalski"}'
curl -i -X POST http://localhost:8080/category -H 'Content-Type: application/json' -d '{"name":"Programowanie"}'
curl -i -X POST http://localhost:8080/member -H 'Content-Type: application/json' -d '{"firstName":"Anna","lastName":"Nowak","email":"anna@example.com"}'
curl -i -X POST http://localhost:8080/book -H 'Content-Type: application/json' -d '{"isbn":"9780306406157","title":"Przykladowa ksiazka","publicationYear":2025,"authorId":1,"categoryIds":[1]}'
curl -i -X POST http://localhost:8080/loans -H 'Content-Type: application/json' -d '{"bookId":1,"memberId":1}'
curl -i -X POST http://localhost:8080/loans/1/return
```

Błędy walidacji i reguł biznesowych są zwracane jako JSON z kodem HTTP, np. `400`, `404` lub `409`.

## Testy

```bash
./mvnw test
```
