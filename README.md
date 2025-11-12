# 🚀 Multi-Validator API

API complète de validation Email, Phone et IBAN avec détection de domaines jetables, rate limiting et documentation Swagger.

## 📋 Fonctionnalités

### ✉️ Email Validation
- Validation syntaxe RFC 5322
- Vérification DNS/MX records
- Détection de 71 000+ domaines jetables
- Détection comptes rôles (admin@, noreply@)
- Suggestions typos (gmai.com → gmail.com)
- Risk scoring (0-100)

### 📱 Phone Validation
- Support 180+ pays
- Validation format international
- Détection type ligne (mobile, fixe, VOIP)
- Formatage E.164, national, international
- Géolocalisation par pays

### 🏦 IBAN Validation
- Support 89 pays SEPA
- Validation MOD-97
- Extraction codes bancaires
- Formatage avec espaces

### 🎯 Fonctionnalités avancées
- Batch validation (jusqu'à 1000 emails)
- Combo validation (email + phone)
- Cache Caffeine (7 jours)
- Rate limiting configurable
- Historique PostgreSQL
- Métriques Prometheus

## 🔧 Technologies

- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: PostgreSQL 15
- **Cache**: Caffeine
- **Documentation**: Swagger/OpenAPI 3
- **Validation**: libphonenumber, iban4j, commons-validator
- **Monitoring**: Spring Actuator, Prometheus

## 📊 Endpoints

### Email
- `POST /api/v1/validate/email` - Validation email
- `GET /api/v1/validate/email?email=test@example.com`

### Phone
- `POST /api/v1/validate/phone` - Validation téléphone
- `GET /api/v1/validate/phone?phone=+33612345678&country=FR`

### IBAN
- `POST /api/v1/validate/iban` - Validation IBAN
- `GET /api/v1/validate/iban?iban=FR7630006000011234567890189`

### Avancé
- `POST /api/v1/validate/batch/email` - Validation batch
- `POST /api/v1/validate/combo` - Validation combo email+phone

### Monitoring
- `GET /api/v1/stats` - Statistiques API
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Métriques

## 📖 Documentation

Swagger UI : `https://votre-api.onrender.com/swagger-ui.html`

## 🚀 Déploiement

### Prérequis
- Java 17+
- Maven 3.8+
- PostgreSQL 15+

### Installation locale

