# 🪑 Rento — Party & Event Equipment Rental Platform

> A full-stack Spring Boot web application for renting party and event equipment.  
> Built for CSE 3220 Software Engineering Lab — complete professional workflow demonstration.

---

## 🔗 Links

| Resource | URL |
|----------|-----|
| Live App | `https://rento-e8w1.onrender.com/` |
| GitHub Repo | `https://github.com/commoner02/Rento` |

---

## 📐 Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Thymeleaf Views                   │
│          (templates/auth, products, rentals)        │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP
┌──────────────────────▼──────────────────────────────┐
│                  Controllers (4)                    │
│  AuthController │ ProductController │ RentalController │ AdminController
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  Service Layer                      │
│  UserService │ ProductService │ RentalService │ PaymentService
└──────────────────────┬──────────────────────────────┘
                       │ JPA
┌──────────────────────▼──────────────────────────────┐
│              Repository Layer (JPA)                 │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│             PostgreSQL Database                     │
└─────────────────────────────────────────────────────┘
```

**Tech Stack:** Java 17 · Spring Boot 3.2 · Spring Security · Thymeleaf · Spring Data JPA · PostgreSQL · Maven · Docker · GitHub Actions

---

## 🗄️ ER Diagram

```
users ──────────────── rentals ────────────── rental_items ──── products
  id (PK)                id (PK)                 id (PK)           id (PK)
  email (unique)         user_id (FK) ←          rental_id (FK) ←  name
  password               rental_date             product_id (FK) ←  category
  first_name             return_date             quantity           daily_rate
  last_name              actual_return_date      daily_rate_at_     total_quantity
  phone                  status                  rental             available_quantity
  role                   total_amount            returned_qty       replacement_value
  created_at             deposit_amount          condition_notes
                         notes                   damage_fee
                         created_at
                              │
                              └──── payments
                                      id (PK)
                                      rental_id (FK)
                                      amount
                                      payment_type
                                      payment_method
                                      status
                                      transaction_date
```

**Relationships:**
- `users` → `rentals` : One-to-Many (1:M)
- `rentals` → `rental_items` : One-to-Many (1:M)
- `products` → `rental_items` : One-to-Many (1:M)
- `rentals` → `payments` : One-to-One (1:1)
- `users` ↔ `products` : Many-to-Many (via rental_items)

---

## 🔑 Roles & Permissions

| Role | Permissions |
|------|-------------|
| **ADMIN** | Manage products (CRUD), manage users & roles, view all rentals, view payments, process refunds |
| **SELLER** | Confirm/activate rentals, process returns, log damage fees, view all rentals |
| **BUYER** | Browse products, create rentals, view own rentals, cancel own rentals |

---

## 🌐 API Endpoints

### Auth
| Method | URL | Access | Description |
|--------|-----|--------|-------------|
| GET | `/auth/login` | Public | Login page |
| POST | `/auth/login` | Public | Process login |
| GET | `/auth/register` | Public | Register page |
| POST | `/auth/register` | Public | Create account |
| POST | `/auth/logout` | Any | Logout |

### Products
| Method | URL | Access | Description |
|--------|-----|--------|-------------|
| GET | `/products` | Authenticated | List all products |
| GET | `/products/{id}` | Authenticated | Product detail |
| GET | `/products/new` | ADMIN | Create product form |
| POST | `/products/new` | ADMIN | Create product |
| GET | `/products/edit/{id}` | ADMIN | Edit product form |
| POST | `/products/edit/{id}` | ADMIN | Update product |
| POST | `/products/delete/{id}` | ADMIN | Delete product |

### Rentals
| Method | URL | Access | Description |
|--------|-----|--------|-------------|
| GET | `/rentals` | Authenticated | List rentals (buyers: own; others: all) |
| GET | `/rentals/{id}` | Owner/SELLER/ADMIN | Rental detail |
| GET | `/rentals/new` | BUYER | New rental form |
| POST | `/rentals/new` | BUYER | Create rental |
| POST | `/rentals/{id}/confirm` | SELLER/ADMIN | Confirm rental |
| POST | `/rentals/{id}/activate` | SELLER/ADMIN | Activate rental |
| POST | `/rentals/{id}/cancel` | Owner/SELLER/ADMIN | Cancel rental |
| GET | `/rentals/{id}/return` | SELLER/ADMIN | Return form |
| POST | `/rentals/{id}/return` | SELLER/ADMIN | Process return |

### Admin
| Method | URL | Access | Description |
|--------|-----|--------|-------------|
| GET | `/admin/users` | ADMIN | Manage users |
| POST | `/admin/users/{id}/role` | ADMIN | Update user role |
| POST | `/admin/users/{id}/delete` | ADMIN | Delete user |
| GET | `/admin/payments` | ADMIN | View all payments |
| POST | `/admin/payments/{id}/refund` | ADMIN | Refund payment |

---

## ▶️ Run Locally

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repo
git clone https://github.com/commoner02/rento.git
cd rento

# Create environment file
cp .env.example .env
# Edit .env with your values if needed

# Start everything
docker compose up --build

# App runs at http://localhost:8080
```

### Option 2: Local Maven (PostgreSQL required)

```bash
# Create PostgreSQL database
createdb rentodb

# Set environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/rentodb
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# Run
mvn spring-boot:run
```

### Demo Accounts

| Email | Password | Role |
|-------|----------|------|
| admin@rento.com | admin123 | ADMIN |
| seller@rento.com | seller123 | SELLER |
| buyer@rento.com | buyer123 | BUYER |

---

## 🧪 Running Tests

```bash
# Run all tests (uses H2 in-memory database — no PostgreSQL needed)
mvn test

# Run only unit tests
mvn test -Dtest="*ServiceTest"

# Run only integration tests
mvn test -Dtest="*ControllerTest"
```

**Test coverage:**
- **24 unit tests** across `UserServiceTest`, `ProductServiceTest`, `RentalServiceTest`
- **11 integration tests** across `ProductControllerTest`, `RentalControllerTest`
- Tests run with H2 in-memory database (no external dependencies)

---

## 🔁 CI/CD Pipeline

```
Push to develop/main
        │
        ▼
  [GitHub Actions]
        │
        ├─ Checkout code
        ├─ Setup JDK 17
        ├─ Run tests (H2, no DB needed)
        ├─ Build JAR
        └─ On main push → Trigger Render deploy
```

**Setup:**
1. In your GitHub repo → Settings → Secrets, add:
   - `RENDER_API_KEY` — from Render account settings
   - `RENDER_SERVICE_ID` — from your Render service URL

---

## 🌐 Deployment (Render)

1. Push to GitHub
2. Create a new **Web Service** on [render.com](https://render.com)
3. Connect your GitHub repo
4. Set build command: `mvn clean package -DskipTests`
5. Set start command: `java -jar target/rento-0.0.1-SNAPSHOT.jar`
6. Add **PostgreSQL** service on Render, then add env vars:
   - `DATABASE_URL` — from Render PostgreSQL connection string
   - `DB_USERNAME` — your DB user
   - `DB_PASSWORD` — your DB password

---

## 🌿 Git Workflow

```
main          ← production, protected (no direct push)
  └── develop ← integration branch
        ├── feature/user-auth
        ├── feature/product-crud
        ├── feature/rental-flow
        └── feature/admin-panel
```

- All features developed on `feature/*` branches
- PR required to merge into `main`
- At least 1 review approval required
- CI must pass before merge

---

## 📁 Project Structure

```
rento/
├── src/main/java/com/rento/
│   ├── RentoApplication.java
│   ├── config/          # SecurityConfig, DataInitializer
│   ├── controller/      # Auth, Product, Rental, Admin
│   ├── dto/request/     # RegisterRequest, ProductRequest, RentalRequest
│   ├── exception/       # Global handler + custom exceptions
│   ├── model/           # User, Product, Rental, RentalItem, Payment
│   ├── repository/      # JPA repositories
│   ├── security/        # UserDetailsServiceImpl
│   └── service/         # Business logic layer
├── src/main/resources/
│   ├── application.properties
│   ├── static/css/main.css
│   └── templates/       # Thymeleaf HTML templates
├── src/test/
│   ├── java/com/rento/
│   │   ├── service/     # Unit tests (Mockito)
│   │   └── controller/  # Integration tests (MockMvc + H2)
│   └── resources/
│       └── application-test.properties  # H2 config
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .github/workflows/ci-cd.yml
└── pom.xml
```

---

## 👥 Team

| Name | Student ID | Role |
|------|-----------|------|
| Sree Shuvo Kumar Joy | 2107116 | Backend + Security |
| Saleheen Uddin Sakin | 2107103 | Frontend + Testing |

