# shaker

Mixology social app — a cocktail Letterboxd: diary, recipes, guidelines, events.

Spring Boot API + Vue 3 SPA, built and served together, with session-based auth and roles.

## Stack

| Layer    | Choice                                                              |
|----------|--------------------------------------------------------------------|
| Backend  | Spring Boot 3.3 (Java 21), Spring Web, Spring Security, Validation |
| Database | MongoDB — embedded in-process by default (`local`), Atlas via the `cloud` profile |
| Frontend | Vue 3 + Vite + Vue Router + Pinia + axios                        |
| Build    | Maven; `frontend-maven-plugin` builds the SPA into the jar        |

## Requirements

- JDK 21 (`JAVA_HOME` must point at a 21 JDK — the repo is not tested on newer)
- Maven 3.9+
- Internet on first build — downloads Node 22 for the SPA build, and (for `local` runs)
  a `mongod` binary the embedded MongoDB caches under `~/.m2`

## Run everything with one command

```bash
JAVA_HOME=/path/to/jdk-21 mvn spring-boot:run
```

Starts on **http://localhost:8080**, serving the API and the built SPA at the same origin.
With no profile set the **`local`** profile is active: an in-process MongoDB (flapdoodle)
starts automatically on `localhost:27017` — no install, no account, no network. Its data
is **wiped on every restart**.

Seeded on startup (idempotent; `app.seed=false` disables):

- three starter guidelines, eight starter ingredients, and three classic recipes (Negroni,
  Old Fashioned, Daiquiri)
- an admin user, **only if** `APP_ADMIN_PASSWORD` is set

Skip the SPA build (API only, faster): add `-DskipFrontend=true`.

### Inspecting the local database

While the app is running, point `mongosh` or MongoDB Compass at `mongodb://localhost:27017`
(database `shaker`) — no credentials. It's only reachable while the app is up.

## Persistent database — the `cloud` profile (MongoDB Atlas)

1. Create a free M0 cluster at <https://cloud.mongodb.com>.
2. **Database Access** → add a user. **Network Access** → add your IP (or `0.0.0.0/0` for
   a throwaway side-project cluster).
3. **Connect → Drivers → Java** → copy the URI; ensure it has the `/shaker` database segment.
4. Put it in a git-ignored `.env` in the project root:
   ```
   MONGODB_URI="mongodb+srv://USER:PASS@cluster0.xxxxx.mongodb.net/shaker?retryWrites=true&w=majority&appName=Cluster0"
   # optional admin seed
   APP_ADMIN_PASSWORD=something-strong
   APP_ADMIN_USERNAME=me
   ```
   `spring-dotenv` loads `.env` automatically — no `export` needed. `*.env` is git-ignored;
   real deployments set real env vars.
5. Run with the profile:
   ```bash
   SPRING_PROFILES_ACTIVE=cloud mvn spring-boot:run
   ```
   `cloud` requires `MONGODB_URI` and disables the embedded MongoDB.

## Frontend dev with hot reload

Run the SPA separately against the running API:

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173, proxies /api -> :8080
```

## App functionality

- **Accounts** — signup/login/logout (session cookie), a profile with preferences: favorite
  spirit, whether to see community guideline tips, preferred display unit (oz/ml).
- **Diary** — log a drink you made yourself or ordered somewhere, with a rating, notes, and date.
- **Recipes** — browse public recipes (app-seeded classics + other users' public ones);
  create your own; edit one of your own recipes as a **new version** (kept in the same
  lineage/family); **fork** any visible recipe (classic or someone else's) into your own
  new lineage. Every recipe gets computed fields: an ABV% estimate, a calorie estimate
  (calculated from ingredients, or entered manually), base-spirit tags (for filtering,
  e.g. "gin"), rolled-up dietary flags (vegan, dairy-free, ...), and a **generated glass
  illustration spec** — a drawn (not AI-generated) glass shape filled with ingredient
  colors, blended for shaken/stirred drinks or kept as distinct layered bands for layered
  ones.
- **Bar** — track the ingredients and equipment (jigger, shaker, ...) you own; check any
  recipe's `/availability` to see if you have everything it needs.
- **Bookmarks** — save other people's/classic public recipes you want to try later.
- **Collections** — named, public-or-private lists of recipes (Letterboxd-style).
- **Guidelines** — articles, videos, pro tips, and community-submitted tips, taggable by
  spirit and moderated (pending/approved/rejected) before going live.
- **Roles & admin** — `USER`/`ADMIN`. Admins get a panel (`/admin/*` in the SPA) to manage
  user roles, the ingredient catalog, guidelines (incl. moderation), and recipe oversight
  (unpublish anything, or author new official classics).

## API

Base path `/api`. Auth is a session cookie (`JSESSIONID`); mutating requests need the
`X-XSRF-TOKEN` header echoing the `XSRF-TOKEN` cookie (axios does this automatically;
do one GET first to receive the cookie). Request/response bodies aren't detailed here —
see the DTOs under `src/main/java/com/shaker/dto/`.

### Auth (`/api/auth`) — public

| Method | Path      |
|--------|-----------|
| POST   | `/signup` |
| POST   | `/login`  |
| POST   | `/logout` |
| GET    | `/session` |

### Account (`/api/account`) — authenticated

| Method | Path |
|--------|------|
| GET    | `/`  |
| PUT    | `/`  |

### Diary (`/api/diary`) — authenticated, own entries only

| Method | Path      |
|--------|-----------|
| GET    | `/`       |
| POST   | `/`       |
| GET    | `/{id}`   |
| DELETE | `/{id}`   |

### Guidelines (`/api/guidelines`) — reads public, writes `ROLE_ADMIN`

| Method | Path                       |
|--------|----------------------------|
| GET    | `/`                        |
| GET    | `/{slug}`                  |
| POST   | `/`                        |
| PUT    | `/{id}`                    |
| PUT    | `/{id}/moderation-status`  |
| DELETE | `/{id}`                    |

### Ingredients (`/api/ingredients`) — reads public, writes `ROLE_ADMIN`

| Method | Path      |
|--------|-----------|
| GET    | `/`       |
| GET    | `/{id}`   |
| POST   | `/`       |
| PUT    | `/{id}`   |
| DELETE | `/{id}`   |

### Recipes (`/api/recipes`) — public recipes are readable by anyone; everything else authenticated/owner-only

Admin capabilities live on this same resource, gated by **role, not a separate path** —
`?all=true` and `?asClassic=true` only take effect for `ROLE_ADMIN` callers and are
silently ignored otherwise.

| Method | Path                 | Notes                                                    |
|--------|----------------------|-----------------------------------------------------------|
| GET    | `/`                  | public recipes; `?baseSpirit=gin` filter; `?all=true` (admin) → every recipe, any visibility |
| GET    | `/mine`              | your own recipes, any visibility (auth)                   |
| GET    | `/{id}`              | public, or your own if private/unlisted                   |
| GET    | `/{id}/availability` | "do I have all the ingredients" (auth)                     |
| POST   | `/`                  | create a new original (auth); `?asClassic=true` (admin) → official classic, forced PUBLIC, no owner |
| POST   | `/{id}/versions`     | save an edit as a new version of your own recipe          |
| POST   | `/{id}/fork`         | fork any visible recipe into your own new lineage         |
| PUT    | `/{id}`              | edit your own recipe in place                              |
| PUT    | `/{id}/visibility`   | change your own recipe's visibility, **or** (admin) override anyone's |
| DELETE | `/{id}`              | owner only                                                 |

### Bar (`/api/bar`) — authenticated, own inventory only

| Method | Path      |
|--------|-----------|
| GET    | `/`       |
| POST   | `/`       |
| DELETE | `/{id}`   |

### Bookmarks (`/api/bookmarks`) — authenticated, own bookmarks only

| Method | Path            |
|--------|-----------------|
| GET    | `/`             |
| POST   | `/`             |
| DELETE | `/{recipeId}`   |

### Collections (`/api/collections`) — public collections readable by anyone; the rest authenticated/owner-only

| Method | Path                        |
|--------|-----------------------------|
| GET    | `/`                         |
| GET    | `/mine`                     |
| POST   | `/`                         |
| PUT    | `/{id}`                     |
| POST   | `/{id}/recipes/{recipeId}`  |
| DELETE | `/{id}/recipes/{recipeId}`  |
| DELETE | `/{id}`                     |

### Users (`/api/users`) — `ROLE_ADMIN` only (no public user directory yet)

| Method | Path                              | Notes                                                   |
|--------|-----------------------------------|-----------------------------------------------------------|
| GET    | `/`                                | all users                                                  |
| POST   | `/{username}/roles/{role}`        | grant a role                                               |
| DELETE | `/{username}/roles/{role}`        | revoke a role (can't revoke USER or self-revoke ADMIN)     |

There is deliberately no `/api/admin/**` namespace — admin actions live on the same
resources as everything else (`/api/users`, `/api/recipes/**`), authorized by role.

## Roles

`Role` enum: `USER`, `ADMIN`. Authorities are `ROLE_USER` / `ROLE_ADMIN`. Access rules live
in `SecurityConfig`, evaluated in order (first match wins):

- `/api/auth/**` — permit all
- `GET /api/guidelines/**`, `GET /api/ingredients/**` — permit all
- `GET /api/recipes/mine`, `GET /api/recipes/*/availability`, `GET /api/collections/mine` — authenticated
- `GET /api/recipes/**`, `GET /api/collections/**` — permit all
- `/api/users/**` — `hasRole("ADMIN")`
- `POST`/`PUT`/`DELETE` on `/api/ingredients/**`, `/api/guidelines/**` — `hasRole("ADMIN")`
- everything else under `/api/**` — authenticated (fine-grained owner-vs-admin checks for
  things like `/api/recipes/{id}/visibility` happen inside the controller/service, the same
  way plain ownership checks already do for `PUT /api/recipes/{id}`)
- static SPA assets and client-side routes — permit all

## Entity structure

MongoDB `@Document` entities under `entity/`, split into subpackages by domain. Every
top-level document extends `common.BaseEntity` (`id: String`, `createdAt: Instant`);
entities that always belong to exactly one user also extend `common.UserOwnedEntity`
(adds `ownerUsername: String`) — `Recipe` deliberately does not, since its owner is
nullable for app-seeded classics.

**`common`** — `Visibility` (enum: `PRIVATE`, `UNLISTED`, `PUBLIC`)

**`user`**
- `User` (`BaseEntity`): `username: String`, `email: String`, `passwordHash: String`,
  `displayName: String`, `bio: String`, `roles: Set<Role>`, `preferences: UserPreferences`
- `Role` (enum): `USER`, `ADMIN`
- `UserPreferences` (embedded): `favoriteSpirit: String`, `showCommunityTips: boolean`,
  `preferredVolumeUnit: VolumeUnit`
- `VolumeUnit` (enum): `OZ`, `ML`

**`diary`**
- `DiaryEntry` (`UserOwnedEntity`): `drinkName: String`, `rating: Integer`, `notes: String`,
  `loggedOn: LocalDate`

**`guideline`**
- `Guideline` (`BaseEntity`): `slug: String`, `title: String`, `category: String`,
  `body: String`, `sortOrder: int`, `contentType: GuidelineContentType`,
  `authorType: GuidelineAuthorType`, `authorUsername: String`, `videoUrl: String`,
  `relatedRecipeId: String`, `relatedSpiritTags: Set<String>`,
  `moderationStatus: ModerationStatus`, `helpfulCount: int`
- `GuidelineContentType` (enum): `ARTICLE`, `VIDEO`, `PRO_TIP`, `COMMUNITY_TIP`
- `GuidelineAuthorType` (enum): `EDITORIAL`, `VERIFIED_PRO`, `COMMUNITY`
- `ModerationStatus` (enum): `PENDING`, `APPROVED`, `REJECTED`

**`recipe`**
- `Recipe` (`BaseEntity`): `name: String`, `recipeFamilyId: String`,
  `forkedFromRecipeId: String`, `origin: RecipeOrigin`, `createdBy: String`,
  `visibility: Visibility`, `category: RecipeCategory`, `baseSpiritTags: Set<String>`,
  `glass: Glass`, `ice: IceStyle`, `method: PreparationMethod`,
  `ingredients: List<IngredientLine>`, `garnishes: List<Garnish>`,
  `instructions: List<String>`, `servings: int`, `abvEstimate: Double`,
  `tasteProfile: Set<TasteNote>`, `dietaryFlags: Set<String>`, `difficulty: Difficulty`,
  `tags: Set<String>`, `description: String`, `photos: List<String>`,
  `generatedImageSpec: GeneratedImageSpec`, `calorieEstimate: Integer`,
  `calorieSource: CalorieSource`, `ratingAverage: double`, `ratingCount: int`,
  `version: int`, `updatedAt: Instant`
- `Ingredient` (`BaseEntity`): `name: String`, `aliases: Set<String>`,
  `category: IngredientCategory`, `subCategory: String`, `abvPercent: double`,
  `colorHex: String`, `opacity: Opacity`, `relativeDensity: Double`,
  `allergenTags: Set<String>`, `caloriesPerOz: Double`
- `IngredientLine` (embedded): `ingredientRef: String`, `freeTextName: String`,
  `role: IngredientRole`, `amount: Double`, `unit: MeasurementUnit`,
  `preparationNote: String`, `optional: boolean`, `sequence: int`
- `Garnish` (embedded): `description: String`, `type: GarnishType`
- `GeneratedImageSpec` (embedded): `glass: Glass`, `fillLevel: double`,
  `colorBands: List<ColorBand>`, `iceOverlay: IceStyle`, `garnishIcon: GarnishType`,
  `carbonationOverlay: boolean`, `renderedAssetUrl: String`
- `ColorBand` (embedded): `colorHex: String`, `proportion: double`
- Enums: `RecipeOrigin` (`APP_CLASSIC`, `USER_ORIGINAL`, `USER_FORK`), `RecipeCategory`,
  `Glass`, `IceStyle`, `PreparationMethod`, `TasteNote`, `Difficulty`, `CalorieSource`
  (`CALCULATED`, `USER_ENTERED`), `IngredientCategory`, `Opacity`, `IngredientRole`,
  `MeasurementUnit`, `GarnishType`

**`bar`**
- `UserBarItem` (`UserOwnedEntity`): `itemType: BarItemType`, `ingredientRef: String`,
  `equipment: Equipment`
- `Bookmark` (`UserOwnedEntity`): `recipeId: String`
- `RecipeCollection` (`UserOwnedEntity`): `name: String`, `description: String`,
  `visibility: Visibility`, `recipeIds: List<String>`, `updatedAt: Instant`
- `BarItemType` (enum): `INGREDIENT`, `EQUIPMENT`
- `Equipment` (enum): `SHAKER`, `JIGGER`, `MUDDLER`, `BAR_SPOON`, `STRAINER`,
  `FINE_STRAINER`, `BLENDER`, `CHANNEL_KNIFE`, `PEELER`, `MIXING_GLASS`, `BLOWTORCH`,
  `ICE_MOLD`, `OTHER`

## Layout

Layered packages (not feature folders): every domain goes controller → service →
repository, and controllers never touch repositories directly.

```
src/main/java/com/shaker/
  controller/   REST endpoints
  service/      business logic (incl. RecipeCompositionCalculator - ABV/calorie/
                image-spec math, kept separate from RecipeService for testability)
  repository/   Spring Data MongoDB repositories
  entity/       @Document models, in subpackages: common/ user/ recipe/ diary/ guideline/ bar/
  dto/          request/response records, all suffixed `...Dto`
  exception/    domain exceptions + @RestControllerAdvice error handler
  security/     SecurityConfig, AppUserDetailsService, AppUserPrincipal, SPA CSRF wiring
  config/       SPA fallback routing, data seeding
src/test/java/  unit tests (Mockito, no Spring context)      -> mvn test
src/it/java/    integration tests (*IT, @SpringBootTest)      -> mvn verify
frontend/       Vue 3 SPA (built into src/main/resources/static by Maven)
  src/views/admin/  admin-only screens (users, ingredients, guidelines, recipes)
  src/constants/enums.js  mirrors backend enums for <select> options - keep in sync
```

## Tests & coverage

```bash
mvn -DskipFrontend=true test     # unit tests (Surefire) + unit coverage report
mvn -DskipFrontend=true verify   # + integration tests (Failsafe) + integration coverage report
```

- **Unit tests** — `src/test/java`, plain Mockito, no Spring context or DB. Always run.
  112 tests as of this writing, covering every service and controller.
- **Integration tests** — `src/it/java/**/*IT.java`, `@SpringBootTest` against a real
  MongoDB. `AuthFlowIT` only runs when `MONGODB_URI` is set — point it at a throwaway
  database (`.../shaker_test`); it calls `deleteAll`. Skipped otherwise.
  ```bash
  MONGODB_URI="mongodb+srv://USER:PASS@cluster0.xxxxx.mongodb.net/shaker_test?retryWrites=true&w=majority" \
    mvn -DskipFrontend=true verify
  ```

**JaCoCo** produces static HTML reports (not a served endpoint):

| Report       | Path                              | Produced by  |
|--------------|-----------------------------------|--------------|
| Unit         | `target/site/jacoco/index.html`   | `mvn test`   |
| Integration  | `target/site/jacoco-it/index.html`| `mvn verify` |

Open with `open target/site/jacoco/index.html`. Lombok-generated accessors are excluded
(`lombok.config`). Unit coverage is ~68% overall — service/security/dto/exception layers
sit near 100%; `SecurityConfig`, `DataSeeder` and the SPA config are only exercised by the
(DB-gated) integration tests.
