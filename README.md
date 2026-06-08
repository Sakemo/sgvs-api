# 🏢 SGVS API - Simplified Sales Management System

[![Status](https://img.shields.io/badge/Status-In%20Development-yellow?style=flat-square&logo=checkmark)](https://github.com)
[![Java Version](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-green?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

---

## 📋 Description

**SGVS API** is a robust RESTful backend application built with **Spring Boot** that provides complete management capabilities for sales, inventory, customers, and expenses. Designed to deliver a "CEO experience" with business intelligence, the API integrates real-time inflation tracking and delivers analytical reports to help small and medium-sized businesses optimize their financial operations.

---
### API Response in Insomnia
Here's an example of an authenticated request successfully returning a product list:

```
GET http://localhost:8081/api/v1/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

✅ Status: 200 OK

{
  "data": [
    {
      "id": 1,
      "name": "Product A",
      "sku": "SKU-001",
      "cost": 50.00,
      "price": 99.90,
      "profitMargin": 99.80,
      "stock": 150,
      "category": "Electronics"
    },
    {
      "id": 2,
      "name": "Product B",
      "sku": "SKU-002",
      "cost": 30.00,
      "price": 79.90,
      "profitMargin": 166.33,
      "stock": 45
    }
  ],
  "meta": {
    "total": 2,
    "page": 0,
    "limit": 20
  }
}
```

> 💡 **Tip:** All responses follow consistent RESTful patterns with appropriate HTTP status codes (200 OK, 400 Bad Request, 401 Unauthorized, 500 Server Error).

---

## 🛠 Technologies Used

### **Backend**
- **Java 21** — Modern language with cutting-edge features
- **Spring Boot 3.5.3** — Web framework and security
- **Spring Data JPA** — Object-relational mapping (ORM)
- **Spring Security + JWT** — Stateless authentication with tokens
- **OAuth2 Google** — Integrated social login

### **Database**
- **PostgreSQL** — Robust relational database
- **Hibernate** — ORM for automatic persistence

### **External Integration**
- **Google OAuth 2.0** — Authentication via Google

### **Tools & Quality**
- **Maven** — Dependency management and build automation
- **Lombok** — Reduces boilerplate (getters, setters, constructors)
- **JUnit 5 + Mockito** — Unit testing with coverage
- **Docker** — Containerization for deployment

---

## ✨ Key Features

### 🔐 **Authentication & Security**
- ✅ User registration and login with robust validation
- ✅ JWT authentication with configurable token expiration
- ✅ Google OAuth 2.0 login with integrated profile
- ✅ Role-based access control (ADMIN, USER)

### 📦 **Product Management**
- ✅ Complete CRUD operations on products
- ✅ Automatic **profit margin** calculation
- ✅ Inventory control (global, per-item, or disabled)
- ✅ Categorization and unique SKU
- ✅ ABC report (top-selling products)

### 👥 **Customer Management**
- ✅ Complete CRUD for customers with contact validation
- ✅ Advanced filters and pagination
- ✅ Purchase history tracking
- ✅ Support for generic customer (sale without specific customer)

### 💰 **Sales Management**
- ✅ Sales recording with multiple items
- ✅ Automatic calculation of totals (gross and net)
- ✅ Per-item discount control
- ✅ Automatic inventory updates
- ✅ Sales reports by period

### 💸 **Expense Management**
- ✅ Registration of operational expenses
- ✅ Restocking expenses (automatically update inventory)
- ✅ Expense categorization
- ✅ Advanced filters and reports

### 📊 **Dashboard & Reports**
- ✅ Summary metric cards (revenue, profit, inventory)
- ✅ Sales evolution charts
- ✅ Product ABC report
- ✅ Integration with Central Bank inflation indices

### 🌍 **Global Settings**
- ✅ Inventory control modes (global, per-item, or disabled)
- ✅ Currency and localization
- ✅ Business parameters (default margins, etc.)

---

## 🎓 The Process and Learnings

### **The Process**

We started by designing the database schema based on a real-world sales management business model. The greatest challenge was balancing **ease of use** with **business robustness** — many startups fail because they over-engineer simple things.

The first major milestone was implementing the **Products and Sales** API with precise profit calculations. We faced initial challenges with:

1. **Automatic inventory updates** — We needed to synchronize sales and restocking expenses without creating inconsistencies. Solution: transactions with `@Transactional` in Spring.
2. **Efficient pagination** — Thousands of records loading slowly. Solution: database indices on PostgreSQL + optimized queries with `@Query` in JPA.
3. **Inflation integration** — We needed to fetch Central Bank indices but with safe fallback. Solution: scheduled task that updates cache periodically.

### **What I Learned**

This project forced me to deeply understand:

- **Spring Security**: Configuring JWT correctly is non-trivial. I now understand why many projects fail with token leaks.
- **Transaction Management**: Operations modifying multiple tables require careful isolation. I learned when to use `@Transactional` and isolation levels.
- **API Design**: Consistent naming, versioning (`/api/v1/`), and standardized responses make ALL the difference in maintainability.
- **Testing Strategy**: Unit tests in services with Mockito revealed bugs that manual testing never caught, especially in calculation logic.
- **Infrastructure**: Docker + PostgreSQL in production is different from h2 in tests. I learned about connectionPool, timeout, and real performance.

---

## 🚀 How to Run the Project
If you just want to **USE** the project, just click on the URL https://sgvs-ui.onrender.com/

### **TO DEVELOPERS**
### **Prerequisites**

You'll need to have installed:

- **Java 21+** ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.8+** (usually comes with IDEs like IntelliJ or Eclipse)
- **PostgreSQL 12+** ([Download](https://www.postgresql.org/download/))
- **Git** to clone the repository

### **Step 1: Clone the Repository**

```bash
git clone https://github.com/your-username/sgvs-api.git
cd sgvs-api
```

### **Step 2: Configure Environment Variables**

1. Copy the example file:
```bash
cp .env.example .env
```

2. Edit the .env file and set real values:

```env
# Database
DB_PASSWORD=your-postgres-password-here

# JWT Secret (generate a random string with at least 32 characters)
# Tip: use `openssl rand -base64 32` to generate automatically
JWT_SECRET_KEY=your-random-32-character-minimum-secret-key-here
```

> ⚠️ **IMPORTANT:** Never commit real passwords! Use environment variables in production.

### **Step 3: Configure PostgreSQL Database**

1. **Create the database:**

```sql
CREATE DATABASE sgvs_db;
CREATE USER postgres WITH PASSWORD 'your-postgres-password-here';
GRANT ALL PRIVILEGES ON DATABASE sgvs_db TO postgres;
```

2. Or use Docker (recommended):

```bash
docker run --name sgvs-postgres \
  -e POSTGRES_PASSWORD=your-password \
  -e POSTGRES_DB=sgvs_db \
  -p 5432:5432 \
  -d postgres:latest
```

### **Step 4: Export Environment Variables**

On **Linux/macOS:**
```bash
export DB_PASSWORD='your-postgres-password-here'
export JWT_SECRET_KEY='your-random-32-character-minimum-secret-key-here'
export GOOGLE_CLIENT_ID='your-google-client-id'
export GOOGLE_CLIENT_SECRET='your-google-client-secret'
```

On **Windows (PowerShell):**
```powershell
$env:DB_PASSWORD = 'your-postgres-password-here'
$env:JWT_SECRET_KEY = 'your-random-32-character-minimum-secret-key-here'
$env:GOOGLE_CLIENT_ID = 'your-google-client-id'
$env:GOOGLE_CLIENT_SECRET = 'your-google-client-secret'
```

### **Step 5: Run the Application**

```bash
# With Maven wrapper (recommended)
./mvnw spring-boot:run

# Or with globally installed Maven
mvn spring-boot:run
```

The application will start at: **`http://localhost:8081`**

### **Step 6: Verify It's Running**

```bash
curl http://localhost:8081/actuator/health

# Expected response:
# {"status":"UP"}
```

### **Step 7: Create Your First Account (Optional)**

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your-username",
    "email": "your-email@example.com",
    "password": "secure-password-here"
  }'
```

---

## 🔌 Environment Variables (.env.example)

The .env.example file is already included in the repository. Copy and configure:

```env
# Database
DB_PASSWORD=your-secure-password-here

# JWT Security
# Generate with: openssl rand -base64 32
JWT_SECRET_KEY=your-32-character-minimum-secret-key-here

# (Optional) Google OAuth
# GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
# GOOGLE_CLIENT_SECRET=your-client-secret
# GOOGLE_REDIRECT_URI=http://localhost:8081/api/auth/google/callback
```

---

## 📦 Build & Deploy

### **Build for Production**

```bash
mvn clean package -DskipTests
```

This generates an executable JAR in `target/sgvs-api-0.0.1-SNAPSHOT.jar`.

### **Run the JAR**

```bash
java -jar target/sgvs-api-0.0.1-SNAPSHOT.jar
```

### **With Docker**

```bash
docker build -t sgvs-api:latest .
docker run -p 8081:8081 \
  -e DB_PASSWORD='your-password' \
  -e JWT_SECRET_KEY='your-secret' \
  sgvs-api:latest
```

---

## 🧪 Testing

Run unit tests:

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

Tests cover main services: `ProductService`, `SaleService`, `CustomerService`, `ExpenseService`.

---

## 📚 Documentation & Resources

- **API Docs:** [Swagger UI] (coming soon)
- **Roadmap:** See README.md for complete phases
- **Project Structure:**
  - controller — REST Controllers
  - `src/main/java/com/flick/business/core/service/` — Business logic
  - entity — JPA Entities
  - repository — Data access

---

## 🤝 Contributing

This is a portfolio project. If you find bugs or have suggestions, open an **issue** or **pull request**.

---

## 📄 License

This project is under the **MIT** license. See the LICENSE file for details.

---

## 👨‍💻 Author

Developed with ❤️ as a modern full-stack business management project.

**Frontend (React + Vite):** [sgvs-ui](https://github.com/your-username/sgvs-ui)

---

## 🔗 Useful Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL](https://www.postgresql.org/)
- [JWT.io](https://jwt.io/)
```
