# Online Restaurant — Order API

API REST Spring Boot pour la prise de commande d'un restaurant en **retrait (PICKUP)**
ou **livraison (DELIVERY)**, avec un **paiement simulé**, en architecture **hexagonale**
(ports & adapters).

> ⚠️ **Application d'entretien technique.** Ce projet est un support d'exercice,
> **volontairement simple** et comportant des **failles assumées** (sécurité notamment) —
> voir [Limites volontaires](#-limites-volontaires-contexte-interview). Ne pas l'utiliser
> tel quel en production.

## Stack

- Java 21, Spring Boot 3.4
- `spring-boot-starter-web`, `data-jpa`, `validation`
- H2 in-memory (zéro configuration)
- springdoc-openapi (Swagger UI)
- Tests : JUnit 5, Mockito, MockMvc, AssertJ

## ⚠️ Limites volontaires (contexte interview)

Ces failles et simplifications sont **connues et volontaires** : les identifier, en discuter
et proposer des correctifs fait partie de l'exercice d'entretien.

### Sécurité

- **Aucune authentification / autorisation** : pas de Spring Security dans le `pom.xml`,
  tous les endpoints sont publics.
- `GET /api/orders` expose **toutes les commandes de tous les clients** (nom, téléphone,
  adresse) — aucune notion de propriétaire.
- N'importe qui peut payer (`POST /{id}/payment`) ou faire avancer le statut
  (`POST /{id}/status`) de n'importe quelle commande — pas de rôles client/staff.
- Console H2 activée et exposée (`/h2-console`), user `sa` **sans mot de passe**.
- Pas de HTTPS, pas de rate limiting, pas de politique CORS restrictive.

### Simplifications fonctionnelles

- Paiement simulé (`FakePaymentGateway`), aucune idempotence de paiement côté API.
- Base H2 in-memory : toutes les données sont perdues au redémarrage.
- Pas de pagination sur les listes (`/api/menu`, `/api/orders`).
- Prix figés au seed, pas de gestion de stock réelle.

## Lancer

```bash
mvn spring-boot:run
```

- API : http://localhost:8080/api
- Swagger UI : http://localhost:8080/swagger-ui.html
- OpenAPI JSON : http://localhost:8080/v3/api-docs
- Console H2 : http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:restaurant`, user `sa`, pas de mot de passe)

Un petit menu est pré-chargé au démarrage : la base est in-memory, les données sont
réinitialisées à chaque redémarrage.

## Tester

```bash
mvn test
```

## Endpoints

| Méthode | Endpoint | Rôle |
|---|---|---|
| GET  | `/api/menu` | lister les items du menu |
| POST | `/api/orders` | créer une commande (PICKUP ou DELIVERY) |
| GET  | `/api/orders` | lister les commandes |
| GET  | `/api/orders/{id}` | consulter une commande |
| POST | `/api/orders/{id}/payment` | régler la commande (paiement simulé) |
| POST | `/api/orders/{id}/status` | faire avancer le statut |

### Cycle de vie d'une commande

```
CREATED ── pay ──▶ PAID ──▶ IN_PREPARATION ──▶ READY ──┬─(PICKUP)──▶ COMPLETED
                                                        └─(DELIVERY)─▶ OUT_FOR_DELIVERY ──▶ DELIVERED
CREATED | PAID ──▶ CANCELLED
```

Les transitions interdites renvoient `409 Conflict`.

### Paiement simulé

Le `FakePaymentGateway` n'effectue aucun appel réseau et est **déterministe** :
toute commande dont le **total se termine par `.13`** (ex. `2.13 €`) est **refusée**
(`402 Payment Required`, la commande reste `CREATED`) ; tout autre montant est encaissé.
Un item « Espresso » à `2.13 €` est seedé exprès pour démontrer ce chemin d'échec.

## Parcours complet en cURL

```bash
# 1. Récupérer un item disponible
ITEM=$(curl -s localhost:8080/api/menu | python3 -c "import sys,json;print(next(i['id'] for i in json.load(sys.stdin) if i['available']))")

# 2. Créer une commande DELIVERY
OID=$(curl -s -X POST localhost:8080/api/orders -H 'Content-Type: application/json' -d "{
  \"type\":\"DELIVERY\",
  \"lines\":[{\"menuItemId\":\"$ITEM\",\"quantity\":2}],
  \"contact\":{\"name\":\"Alice\",\"phone\":\"0600000000\"},
  \"deliveryAddress\":{\"street\":\"1 rue de la Paix\",\"postalCode\":\"75002\",\"city\":\"Paris\"}
}" | python3 -c "import sys,json;print(json.load(sys.stdin)['id'])")

# 3. Payer
curl -s -X POST localhost:8080/api/orders/$OID/payment

# 4. Avancer le statut
for S in IN_PREPARATION READY OUT_FOR_DELIVERY DELIVERED; do
  curl -s -X POST localhost:8080/api/orders/$OID/status -H 'Content-Type: application/json' -d "{\"status\":\"$S\"}"
done
```

## Architecture (hexagonale)

```
com.restaurant.ordering
├── domain                cœur métier, aucune dépendance framework
│   ├── model             Order (agrégat), OrderLine, MenuItem, Money, Payment, enums…
│   └── exception         exceptions métier
├── application
│   ├── port/in           use cases (interfaces d'entrée) + commands
│   ├── port/out          ports de sortie : OrderRepository, MenuRepository, PaymentGateway
│   └── service           implémentations des use cases
└── adapter
    ├── in/web            controllers REST, DTOs, mappers, gestion d'erreurs
    └── out
        ├── persistence   JPA (entités + mappers) implémentant les ports
        └── payment       FakePaymentGateway implémentant PaymentGateway
```

Le domaine ignore Spring et JPA. Persistance et paiement sont des adapters
interchangeables branchés derrière des ports — le `FakePaymentGateway` peut être remplacé
par un vrai PSP sans toucher au cœur métier.

## Logs

SLF4J, une ligne par changement d'état (création, paiement, transition), clé = id de commande,
écrite par le service applicatif. Les erreurs client (4xx) sont tracées une seule fois en `INFO`
par le `GlobalExceptionHandler`, un paiement refusé en `WARN` par le service. Les controllers ne
loguent pas : la trace des requêtes HTTP relève d'un access log ou du tracing, pas de chaque
endpoint. Les coordonnées du client (nom, téléphone, adresse) sont des données personnelles :
elles ne sont jamais loguées.

## Codes d'erreur

| Code | Cas |
|---|---|
| `400` | corps de requête invalide / DELIVERY sans adresse / commande sans ligne |
| `402` | paiement refusé par la passerelle |
| `404` | commande introuvable |
| `409` | item indisponible ou inconnu, transition de statut interdite, double paiement |
