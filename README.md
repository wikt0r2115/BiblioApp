# BiblioApp

REST API do zarządzania książkami, czytelnikami i wypożyczeniami. Projekt używa JDK 25, Spring Boot 4, JPA, Flyway oraz H2 lub PostgreSQL 16. Aktualny kontrakt żądań, odpowiedzi i błędów jest w [openapi.yaml](openapi.yaml).

## Jak działa

Kontrolery obsługują HTTP, serwisy egzekwują reguły wypożyczeń, a repozytoria JPA zapisują encje `Author`, `Category`, `Book`, `Member` i `Loan`. Flyway tworzy schemat; profil `demo` dodaje dane przykładowe. Książka ma jednego autora i dowolną niepustą grupę kategorii. Każde wypożyczenie wskazuje książkę i czytelnika.

Wypożyczyć można tylko dostępną, niearchiwalną książkę aktywnemu czytelnikowi. Wypożyczenie ustawia `available=false`; zwrot ustawia `available=true`. Ponowna próba wypożyczenia lub zwrotu daje HTTP `409`. Archiwizacja książki lub czytelnika z aktywnym wypożyczeniem daje `409`; po zwrocie można archiwizować, a historia pozostaje dostępna przez `/loans/{id}` i `/members/{id}/loans/history`. Archiwalna książka i czytelnik nie są widoczni w zwykłych odczytach. W PostgreSQL transakcje blokują odpowiednie wiersze, a indeks częściowy gwarantuje najwyżej jedno aktywne wypożyczenie książki.

To lokalny przykład backendu bez logowania, kont publicznych i interfejsu użytkownika. Nie wystawiaj go bezpośrednio do publicznej sieci.

## Uruchomienie z H2

Wymagany jest JDK 25. Projekt zawiera Maven Wrapper, więc nie trzeba instalować Mavena.

```bash
./mvnw spring-boot:run
```

Profil `h2` jest domyślny. Baza działa w pamięci i znika po zatrzymaniu aplikacji. API jest dostępne pod `http://localhost:8080`.

## Uruchomienie z PostgreSQL

Wymagany jest Docker z wtyczką Compose. Jeśli nie masz jeszcze pliku `.env`, skopiuj `.env.example` do `.env` i ustaw własne hasło. Plik `.env` jest ignorowany przez Git.

```bash
if [ ! -f .env ]; then
  cp .env.example .env
fi
sudo docker compose up -d --wait
set -a
. ./.env
set +a
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

Warunek na początku chroni istniejący plik `.env` przed nadpisaniem. `sudo` jest potrzebne, gdy użytkownik nie ma dostępu do gniazda Docker; jeśli masz taki dostęp, uruchom `docker compose` bez `sudo`.

PostgreSQL nasłuchuje lokalnie na porcie 5432. Flyway automatycznie uruchamia migracje schematu i indeksu aktywnych wypożyczeń, a Hibernate sprawdza zgodność schematu z encjami. Dane są przechowywane w wolumenie `postgres_data`. `docker compose down` zatrzymuje środowisko i zachowuje dane; dodanie `--volumes` usuwa również bazę.

## Demo w Dockerze

Wymagany jest Docker z wtyczką Compose. Demo uruchamia PostgreSQL i aplikację oraz wypełnia bazę przykładowymi danymi. Nie wymaga pliku `.env`.

```bash
docker compose -f compose.demo.yaml up --build -d --wait
curl 'http://127.0.0.1:8080/book?size=5'
./scripts/smoke-demo.sh  # wymaga curl i jq; wypisuje ID utworzonego wypożyczenia
```

API jest dostępne pod `http://127.0.0.1:8080`. Migracja profilu `demo` tworzy po 100 autorów, kategorii, czytelników, książek, powiązań książka–kategoria i wypożyczeń. Spośród wypożyczeń 20 jest aktywnych. Dane są przechowywane w osobnym wolumenie `biblioapp-demo_demo_postgres_data`; ponowne uruchomienie nie dodaje duplikatów. Skrypt smoke tworzy własne dane, sprawdza konflikt, zwrot, archiwizację i historię. Hasło w pliku Compose służy tylko do lokalnego demo; port bazy nie jest publikowany.

Ponowne uruchomienie aplikacji bez resetowania bazy:

```bash
docker compose -f compose.demo.yaml up -d --no-deps --force-recreate --wait app
```

Kopia i odtworzenie danych demo (w katalogu repozytorium):

```bash
docker compose -f compose.demo.yaml exec -T postgres \
  pg_dump -U biblioapp_demo -d biblioapp_demo -Fc > biblioapp-demo.dump
docker compose -f compose.demo.yaml stop app
docker compose -f compose.demo.yaml exec -T postgres \
  pg_restore -U biblioapp_demo -d biblioapp_demo --clean --if-exists < biblioapp-demo.dump
docker compose -f compose.demo.yaml start --wait app
```

Plik kopii może zawierać dane czytelników; trzymaj go lokalnie. Po odtworzeniu sprawdź `/loans/{id}` dla ID wypisanego przez skrypt smoke. Reset całej bazy demo jest świadomą operacją:

```bash
docker compose -f compose.demo.yaml down
docker compose -f compose.demo.yaml down --volumes
docker compose -f compose.demo.yaml up --build -d --wait
```

Jeśli Docker wymaga uprawnień administratora, dodaj `sudo` przed `docker compose`. Gdy port 8080 jest zajęty, zmień jego lewą stronę w `compose.demo.yaml`. Jeśli Compose zgłasza brak dostępu do demona, uruchom Docker Desktop lub usługę Docker i sprawdź `docker info`. Jeśli aplikacja startuje przed bazą, użyj `up --wait` i sprawdź `docker compose -f compose.demo.yaml logs postgres app`; kontener aplikacji czeka na zdrowy PostgreSQL.

## Obraz wydania

Po wydaniu v1 uruchom konkretny tag obrazu zamiast `latest`:

```bash
BIBLIOAPP_IMAGE=ghcr.io/wikt0r2115/biblioapp:v1.0.0 \
  docker compose -f compose.demo.yaml up -d --no-build --pull always --wait
./scripts/smoke-demo.sh
```

`compose.demo.yaml` używa tego obrazu, gdy ustawisz `BIBLIOAPP_IMAGE`; bez zmiennej buduje kod lokalny. Obraz GHCR musi być publiczny albo wymagać `docker login ghcr.io`.

## CI/CD

Workflow GitHub Actions uruchamia testy H2 i PostgreSQL (Testcontainers) przy pull requestach oraz zmianach na `main` i tagach `v*`. Buduje demo, sprawdza dane seed, pełny przepływ API i trwałość danych po restarcie. Po przejściu testów publikuje obraz jako `sha-<pełny SHA commita>`, na `main` także `latest`, a dla tagu wersji także `v1.0.0`. Ostatni job pobiera opublikowany obraz i uruchamia na nim smoke test. Pakiet GHCR może wymagać ustawienia publicznej widoczności w GitHub, jeśli ma być dostępny bez logowania.

## Najważniejsze endpointy

| Zasób | Operacje |
| --- | --- |
| Autorzy | `POST /author`, `GET /author` |
| Kategorie | `POST /category`, `GET /category` |
| Książki | `POST /book`, `GET /book`, `GET /book/{id}`, `PUT /book/{id}`, `PUT /book/{id}/isbn`, `DELETE /book/{id}` |
| Czytelnicy | `POST /member`, `GET /member/{id}`, `PUT /member/{id}`, `DELETE /member/{id}` |
| Wypożyczenia | `POST /loans`, `GET /loans/{id}`, `POST /loans/{id}/return`, `GET /members/{id}/loans/active`, `GET /members/{id}/loans/history` |

`GET /book` obsługuje parametry `title`, `authorId`, `available`, `page`, `size` i `sort`, np. `/book?title=java&available=true&page=0&size=10&sort=title,asc`. Paginacja zaczyna się od strony 0.

Przykładowy przepływ na pustej bazie (identyfikatory `1` zastąp wartościami otrzymanymi w odpowiedziach; w demo są już dane seed):

```bash
curl -i -X POST http://localhost:8080/author -H 'Content-Type: application/json' -d '{"firstName":"Jan","lastName":"Kowalski"}'
curl -i -X POST http://localhost:8080/category -H 'Content-Type: application/json' -d '{"name":"Programowanie"}'
curl -i -X POST http://localhost:8080/member -H 'Content-Type: application/json' -d '{"firstName":"Anna","lastName":"Nowak","email":"anna@example.com"}'
curl -i -X POST http://localhost:8080/book -H 'Content-Type: application/json' -d '{"isbn":"9780306406157","title":"Przykladowa ksiazka","publicationYear":2025,"authorId":1,"categoryIds":[1]}'
curl -i -X POST http://localhost:8080/loans -H 'Content-Type: application/json' -d '{"bookId":1,"memberId":1}'
curl -i -X POST http://localhost:8080/loans/1/return
curl -i -X DELETE http://localhost:8080/book/1
curl -i -X DELETE http://localhost:8080/member/1
curl -i http://localhost:8080/members/1/loans/history
```

Błędy walidacji i reguł biznesowych są zwracane jako `application/problem+json` z kodem HTTP `400`, `404` lub `409` (pola m.in. `title`, `status`, `detail`).

## Testy

```bash
./mvnw test
```

Testy integracyjne PostgreSQL uruchamiają kontener przez Docker. Obejmują pełny scenariusz REST, wyszukiwanie i paginację, ponowną migrację oraz równoczesne wypożyczenia, zwroty i archiwizację. Uruchom `./mvnw verify` przed wydaniem.
