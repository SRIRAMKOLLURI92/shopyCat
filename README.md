# 🐱 shopyCat

Your neighbourhood in one app: **offers from local shops**, **deal tips from neighbours**, and **used items for sale**, sorted by how close they are to you.

Built with Java 21 and Spring Boot, so it is also a hands-on way to learn Java web development.

## What it does

There are two kinds of accounts:

| | Shop owner | User (shopper) |
|---|---|---|
| Add shops (name, address, hours, location) | ✅ | ❌ |
| Post offers | ✅ "Verified shop offer" for their own shops, or a community tip | ✅ "Community tip" about a deal they spotted |
| Sell used items | ✅ As their shop or as a private seller | ✅ As a private seller |
| Contact a private seller | ✅ By contact request | ✅ By contact request |

Everyone, including visitors who are not logged in, can browse the **Near me** page: tap 📍 Near me to sort offers, used items and shops by distance and filter by category, radius or keyword.

## Privacy rules between shop owners and users

All rules live in one class, [`PrivacyPolicy`](src/main/java/com/shopycat/service/PrivacyPolicy.java), and are covered by tests.

1. **Who can see a post.** Each post picks an audience: *Everyone*, *Members only* (logged in), *Shop owners only* (for example, wholesale or stock clearance between shops), or *Users only* (for example, a private sale between neighbours). The author always sees their own post. A hidden post looks like a "not found" page, so its existence is not revealed.
2. **Names.** People are shown only by their display name. Email and phone are never shown on a post.
3. **Location.** Shop posts use the shop's exact location, because it is a business. Posts by a person are saved with the location rounded to about 1 km, so nobody can find someone's home.
4. **Contact details.** A shop's phone is public. To reach a private seller, you send a contact request. Only when the seller accepts do both sides see each other's email and phone.
5. **Ownership.** Only the author can edit, delete or mark a post as sold, and only a shop's owner can post on behalf of that shop.
6. **Trust labels.** Offers from shops are marked "✔ Verified shop" and offers from users are marked "Community", so shoppers know who is behind each deal.

## Run it

You need Java 21. Maven is included through the wrapper.

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Open http://localhost:8080. On first start, demo data is added (around central Hyderabad). Demo logins, all with password `password123`:

- `owner@shopycat.dev` and `owner2@shopycat.dev`: shop owners
- `user@shopycat.dev` and `user2@shopycat.dev`: users

Data is stored in `./data` (an H2 file database). Delete that folder to start fresh. Set `shopycat.demo-data=false` in `application.properties` to skip demo data.

To use PostgreSQL instead:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/shopycat DATABASE_USER=... DATABASE_PASSWORD=... \
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Run the tests with `./mvnw test`.

## Try it in the browser

`frontend/index.html` is a self-contained web version of the app (same pages and privacy rules, data kept in your browser). Open it directly in a browser.

## Project layout

```
src/main/java/com/shopycat/
  model/     Entities: AppUser, Shop, Offer, UsedListing, ContactRequest, and enums
  repo/      Spring Data repositories
  service/   PrivacyPolicy (all privacy rules), FeedService (near-me feeds), Geo (distance)
  security/  Login, password hashing, current user
  web/       Controllers for pages and forms
  config/    Demo data
src/main/resources/templates/   Thymeleaf pages
frontend/index.html             Browser-only demo of the app
```

## Roadmap ideas

- Photos for offers and items
- Map view (Leaflet + OpenStreetMap)
- Deal alerts ("tell me when shoes are 30% off within 5 km")
- In-app chat instead of sharing contact details
- Admin approval for new shops, and reporting of bad posts
- PostGIS for fast "near me" queries at scale
- A mobile app on top of a REST API
