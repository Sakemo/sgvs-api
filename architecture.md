# Flick Business — Architecture

## 1. Overview

Flick Business is a full-stack business management system designed to replace paper notebooks, loose notes, and manual cash-control practices in small businesses.

The idea came from years of direct experience inside **Frigorífico do Elizeu**, where day-to-day operations relied on cash movement notebooks, IOU notes, and other manual records. In that environment, the main problem was not the absence of software altogether, but the absence of software that was actually practical for a small business. Many sales systems are either too expensive, too heavy, or too generic for the real needs of a small operation.

Flick Business was built to solve that gap.

The system helps business owners and cashiers manage:

* sales and cash flow
* stock and restocking
* customers and balances
* suppliers
* product performance
* profitability and pricing

It is designed around the questions a small business owner actually wants answered at the end of the day, week, month, and year.

### Main stack

* **Frontend:** React + TypeScript + Vite
* **Backend:** Java + Spring Boot
* **Database:** PostgreSQL
* **Auth:** JWT + Google OAuth

---

## 2. System Diagram

### Frontend

The frontend is a modern single-page application built with React, TypeScript, and Vite. It handles:

* login and session persistence
* protected routes
* dashboard visualization
* CRUD screens
* sales flow
* settings and localization
* theme switching

### Backend

The backend is a REST API built with Java and Spring Boot. It handles:

* authentication
* business rules
* persistence
* reports
* sales logic
* inventory updates
* customer balance logic
* dashboard data

### Database

The database is PostgreSQL. It stores the persistent business data, including:

* users
* products
* customers
* suppliers
* sales
* sale items
* expenses
* settings
* categories
* payments

### Auth

Authentication uses:

* **JWT** for session management
* **Google OAuth** as an additional login option

---

## 3. Main Flows

### Login flow

1. The user opens the login page.
2. The frontend sends credentials to the backend.
3. If authentication succeeds, the system stores the token and user data in session storage.
4. Protected routes check whether the user is authenticated before allowing access.
5. Google login is also available through OAuth.

### CRUD flow

The system includes CRUD screens for the main business entities, such as:

* products
* customers
* providers
* expenses
* settings and categories

The frontend pages call REST services exposed by the backend.
The backend validates the request, applies business rules, saves the data, and returns the result to the UI.

### Sales flow

The sales flow is one of the core parts of the system.

1. The cashier opens the sales page.
2. Products and customers are selected.
3. The system calculates totals, margins, discounts, and payment details.
4. The sale is sent to the backend.
5. The backend stores the sale and its items.
6. Reports and stock data are updated as part of the sale logic.

### Inventory update flow

Inventory updates depend on the configured stock control mode.

The system can handle different stock control behaviors:

* global inventory control
* per-item inventory control
* disabled inventory control

When a sale is completed, the backend updates stock accordingly.
When a sale is reversed or deleted permanently, the system can restore inventory and adjust customer balances if needed.

---

## 4. Modules

### Frontend pages

The frontend is organized into route-based pages, including:

* LandingPage
* LoginPage
* RegisterPage
* DashboardPage
* SalesPage
* ExpensesPage
* ReportsPage
* ProductsPage
* ProvidersPage
* CustomersPage
* SettingsPage

### Backend controllers / services / repositories

The backend follows a layered structure:

#### Controllers

* AuthController
* CategoryController
* CustomerController
* DashboardController
* ExpenseController
* GeneralSettingsController
* PaymentController
* ProductController
* ProviderController
* ReportController
* SaleController

#### Services

* AuthenticationService
* ProductService
* CustomerService
* ProviderService
* ExpenseService
* SaleService
* DashboardService
* ReportService
* GeneralSettingsService
* PaymentService
* CategoryService

#### Repositories

* ProductRepository
* CustomerRepository
* ProviderRepository
* ExpenseRepository
* SaleRepository
* SaleItemRepository
* CategoryRepository
* GeneralSettingsRepository
* PaymentRepository

### Shared concerns

#### Auth

Authentication is handled across both frontend and backend:

* frontend: AuthContext, protected routes, login flow, Google login
* backend: AuthController, AuthenticationService, JWT security, OAuth2 Google

#### Theme

The UI supports light and dark themes with persistence.

#### i18n

The frontend supports English and Portuguese through `react-i18next`.

#### Validation

Validation happens in both layers:

* frontend form validation and error feedback
* backend validation using DTOs and request validation annotations

---

## 5. Technical Decisions

### Why React + TypeScript

React was chosen because of prior experience and because it fits well with building a responsive business interface.
TypeScript adds structure and safety, which is important in a system with many forms, entities, and API interactions.

### Why Spring Boot

Spring Boot was chosen for speed of development and for the structure it gives to CRUD-oriented architectures.
It also fits the goal of building a maintainable system with clear separation between controllers, services, and repositories.

### Why JWT

JWT was chosen for stateless authentication and session handling.
It works well for protected routes and API-based applications.

### Why PostgreSQL

PostgreSQL was chosen because it is a strong relational database for business systems that need reliability, consistency, and room for growth.

### Why Java

Java was chosen as the main backend language because it is widely used in the market and has strong long-term stability.
The project was designed with longevity in mind, so the stack should survive both platform changes and time.

---

## 6. Improvements / Roadmap

### CI

Add a continuous integration pipeline to automatically run checks on every push or pull request.

### UI tests

Add frontend automated tests so the interface has coverage for critical flows.

### API tests

Expand backend test coverage for authentication, sales, inventory, and business rules.

### Security

The next major step is security hardening.

### Barcode support

A key future feature is barcode-based product insertion into:

* the products table
* the sales table

This will improve cashier speed and reduce manual entry errors.

### Deploy notes

The project already has a strong foundation for deployment, but deployment documentation and environment setup can still be improved and standardized.

### Final roadmap direction

The project should continue toward:

1. security
2. tests
3. barcode-driven product entry
4. stronger deployment/documentation
5. long-term maintainability
