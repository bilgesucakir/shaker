# shaker

Mixology social app — a cocktail Letterboxd: diary, recipes, guidelines, events.

This is the base skeleton: a Spring Boot API + a Vue 3 SPA, built and served together,
with session-based auth and roles.

## Stack

| Layer    | Choice                                                              |
|----------|--------------------------------------------------------------------|
| Backend  | Spring Boot 3.3 (Java 21), Spring Web, Spring Security, Validation |
| Database | MongoDB Atlas (cloud only — no local database, ever)              |
| Frontend | Vue 3 + Vite + Vue Router + Pinia + axios                        |
| Build    | Maven; `frontend-maven-plugin` builds the SPA into the jar        |

### Why MongoDB

Chosen for fast iteration on document-shaped content (recipes with nested
ingredients/steps, flexible event payloads) while the schema is still moving.
Trade-off to keep in mind: the social graph (follows, likes, feeds) and rating
aggregations are more work than in SQL — revisit if those become central.

## Requirements

- JDK 21 (`JAVA_HOME` must point at a 21 JDK — the repo is not tested on newer)
- Maven 3.9+
- A MongoDB Atlas cluster (the free **M0** tier is fine) — see setup below
- Internet on first build (downloads Node 22 for the frontend build)

## One-time: MongoDB Atlas setup

Nothing runs a database on this machine. You need a cloud cluster:

1. Create a free cluster at <https://cloud.mongodb.com> (M0, any region).
2. **Database Access** → add a database user (username + password).
3. **Network Access** → add your current IP (or `0.0.0.0/0` for a throwaway side-project cluster).
4. **Connect → Drivers → Java** → copy the connection string. It looks like:
   ```
   mongodb+srv://USER:PASS@cluster0.xxxxx.mongodb.net/shaker?retryWrites=true&w=majority&appName=shaker
   ```
   The `/shaker` path segment is the database name — add it if the copied string omits it.

## Local config: `.env`

Create a git-ignored `.env` in the project root (the Atlas onboarding "Download .env"
gives you most of this — add the `/shaker...` path to the URI):

```
MONGODB_URI="mongodb+srv://USER:PASS@cluster0.xxxxx.mongodb.net/shaker?retryWrites=true&w=majority&appName=Cluster0"
# optional: seed an admin user on first boot
APP_ADMIN_PASSWORD=something-strong
APP_ADMIN_USERNAME=me
```

`spring-dotenv` loads this automatically for `mvn spring-boot:run`, tests, and IDE runs —
no `source`/`export` needed. `*.env` is git-ignored. Real deployments set real env vars.

## Run everything with one command

```bash
JAVA_HOME=/path/to/jdk-21 mvn spring-boot:run
```

`MONGODB_URI` is required (from `.env` or the real environment) — the app will not start
without it. This builds the Vue app into `src/main/resources/static/` and starts the API
on **http://localhost:8080**, serving the SPA at the same origin, backed by your Atlas
cluster.

Seeded on startup (idempotent; disable everything with `app.seed=false`):

- three starter guidelines
- an admin user **only if** `APP_ADMIN_PASSWORD` is set

Skip the frontend build (API only, faster): add `-DskipFrontend=true`.

### If it can't reach Atlas

`MongoTimeoutException` with `SSLException: Received fatal alert: internal_error` (TCP to
`:27017` connects but the TLS handshake is rejected) means a network is blocking the
MongoDB wire protocol — **corporate/office Wi-Fi with TLS inspection does this**. It is
not a code or Atlas-config problem. Run from a personal network (phone hotspot, home
Wi-Fi), and add that network's IP under Atlas **Network Access** (or use `0.0.0.0/0` for
a throwaway side-project cluster).

## Frontend dev with hot reload

Run the SPA separately against the running API:

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173, proxies /api -> :8080
```

## API

Base path `/api`. Auth is a session cookie (`JSESSIONID`); mutating requests need the
`X-XSRF-TOKEN` header echoing the `XSRF-TOKEN` cookie (axios does this automatically;
do one GET first to receive the cookie).

### Public (anonymous OK)

| Method | Path                     | Notes                                  |
|--------|--------------------------|----------------------------------------|
| POST   | `/api/auth/signup`       | `{username,email,password,displayName?}` → 201, logs you in |
| POST   | `/api/auth/login`        | `{username,password}` → 200 + session  |
| POST   | `/api/auth/logout`       | 204, invalidates session               |
| GET    | `/api/auth/me`           | 200 account, or 401 if anonymous       |
| GET    | `/api/guidelines`        | list                                   |
| GET    | `/api/guidelines/{slug}` | one                                    |

### Authenticated (any logged-in user)

| Method | Path                | Notes                          |
|--------|---------------------|--------------------------------|
| GET    | `/api/account`      | current profile                |
| PUT    | `/api/account`      | `{displayName?,bio?}`          |
| GET    | `/api/diary`        | your diary entries             |
| POST   | `/api/diary`        | `{drinkName,rating?,notes?,loggedOn?}` |
| GET    | `/api/diary/{id}`   | one of your entries            |
| DELETE | `/api/diary/{id}`   | 204                            |

### Admin (`ROLE_ADMIN`)

| Method | Path               | Notes       |
|--------|--------------------|-------------|
| GET    | `/api/admin/users` | all users   |

## Roles

`Role` enum on the `User` document: `USER`, `ADMIN`. Authorities are `ROLE_USER` /
`ROLE_ADMIN`. The access rules live in `SecurityConfig`:

- `/api/auth/**` and `GET /api/guidelines/**` — permit all
- `/api/admin/**` — `hasRole("ADMIN")`
- everything else under `/api/**` — authenticated
- static SPA assets and client-side routes — permit all

## Layout

```
src/main/java/com/shaker/
  auth/        signup / login / logout / me
  account/     account read + update, shared AccountResponse
  user/        User document, repository, UserService, Role
  security/    SecurityConfig, UserDetailsService, SPA CSRF wiring
  guideline/   public reference content
  diary/       per-user diary entries
  admin/       admin-only endpoints
  common/      error handling
  config/      SPA fallback routing, dev data seeding
frontend/      Vue 3 SPA (built into src/main/resources/static by Maven)
```

## Splitting the frontend out later

The SPA already talks to the API only over `/api` with cookies. To host it
separately: stop building it into the jar (drop the `frontend` Maven profile),
deploy `frontend/` on its own, point it at the API base URL, and tighten
`corsConfigurationSource` in `SecurityConfig` to the real origin.

## Tests

`AuthFlowTest` is an integration test that hits a real MongoDB, so it only runs when
`MONGODB_URI` is set — point it at a **separate throwaway database** (it calls
`deleteAll` on users):

```bash
# skips the DB test
mvn -DskipFrontend=true test

# runs it against a test DB
MONGODB_URI="mongodb+srv://USER:PASS@cluster0.xxxxx.mongodb.net/shaker_test?retryWrites=true&w=majority" \
  mvn -DskipFrontend=true test
```
