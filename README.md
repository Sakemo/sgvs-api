Backend repo for: (https://github.com/Sakemo/sgvs-ui)[flick-business-frontend]

### **Master Project Document: Simplified Sales & Gestião de Vendas (SGVS)**

**Version:** 1.0 (Post-Refactor)
**Last Updated:** July 28, 2025

#### **1. Introduction & Project Vision**

The **Simplified Sales Management System (SGVS)** is a full-stack web application designed to be a powerful, intuitive, and visually impactful management tool for small to medium-sized businesses. The project's core philosophy extends beyond simple transaction logging to provide the user with a "CEO experience," complete with actionable insights and full control over financial and inventory operations. The software aims to transform raw data into business intelligence, with a sharp focus on cash flow, profitability, and product performance.

Built on a modern and scalable architecture, SGVS utilizes React (Vite + TypeScript) on the frontend and Java (Spring Boot) on the backend, with PostgreSQL for relational data. The application was developed following "MIT-level" principles: clean code, decoupled architecture, robust testing, and an exceptional user experience (UX), including internationalization (i18n), a dark/light theme, and reactive UI components. This document serves as the master roadmap for ongoing development, detailing what has been completed and outlining the next strategic steps.

---

### **2. Master Project Roadmap**

✅ = Completed | ⏳ = In Progress / Partially Completed | 🎯 = Next Target | ❌ = Not Started

#### **PHASE 1: Foundation & Architectural Migration (100% ✅)**

This phase focused on rebuilding the application from the ground up to establish a high-quality technical foundation.

*   ✅ **Backend:** Spring Boot project rebuilt with a clean architecture, 100% in English (packages, classes, methods).
*   ✅ **Frontend:** React/Vite project rebuilt with a modern architecture, TypeScript, and reusable UI components based on `CVA`.
*   ✅ **Database:** PostgreSQL schema recreated, 100% in English, managed via `ddl-auto` in development.
*   ✅ **Essential Features Migrated:**
    *   **Products:** Full CRUD, filtering, sorting (including "most sold"), copying, and a details drawer.
    *   **Customers:** Full CRUD, filtering, sorting, a details modal, and confirmation dialogs.
    *   **Expenses:** Full CRUD, filtering, a details drawer, and a conditional UI for **Restocking Expenses** that updates inventory.
    *   **Sales:** Full CRUD, filtering, pagination, summary cards (gross, net), and intelligent autocomplete for products and customers.
    *   **Settings:** Implementation of **Configurable Stock Control** (GLOBAL, PER_ITEM, NONE).
    *   **Dashboard:** All planned cards and charts are functional and connected to the API.
*   ✅ **"MIT-Level" UX:**
    *   Full `i18n` (English/Portuguese).
    *   Global Toast Notification System with `react-hot-toast`.
    *   Global Confirmation Modal for critical actions.
    *   `AdvancedOptions` component to simplify complex forms.
    *   Application-wide Dark/Light theme.
*   ✅ **Code Quality:** Comprehensive unit tests for the backend service layer (`Product`, `Sale`, `Customer`, `Expense`).

---

#### **🎯 PHASE 2: Authentication & Multi-User Support (Next Target)**

This is the next fundamental step to turn the application into a secure product ready for multiple users.

*   🎯 **Backend - Spring Security & JWT:**
    *   **What:** Secure the entire API. Only authenticated users will be able to access the endpoints.
    *   **How:**
        1.  Introduce a `User` entity (`username`, `password`, `roles`).
        2.  Configure Spring Security to use **JWT (JSON Web Tokens)** for stateless authentication.
        3.  Create authentication endpoints: `POST /api/auth/register` and `POST /api/auth/login`.
        4.  Implement a JWT filter that validates the token on every request to protected endpoints.
*   🎯 **Frontend - Login Flow & Protected Routes:**
    *   **What:** Create the login experience and protect application pages.
    *   **How:**
        1.  Create a `LoginPage` and `RegisterPage`.
        2.  Develop an **`AuthContext`** to manage user state (logged in/out) and the JWT across the application.
        3.  Implement a `ProtectedRoute` component to wrap routes in `App.tsx`, redirecting unauthenticated users.
        4.  Update the `apiClient` (request interceptor) to add the `Authorization: Bearer <token>` header to all API calls.

---

#### **PHASE 3: Business Intelligence & User "Magic" (20% ⏳)**

This phase focuses on turning data into even more valuable insights and enhancing user workflows.

*   ✅ **`#8` Profit Margin in Product Table:** Completed.
*   ✅ **`#2` Product ABC Curve Report:** Completed.
*   ⏳ **`#1` Low Stock Alert:** A notification system for products reaching a minimum stock level.
*   ⏳ **`#9` Suggested Selling Price:** System-suggested sale price based on cost and a desired profit margin.
*   ❌ **`#10` Loss Management System:** A feature to record inventory losses (damaged, expired), crucial for accurate Cost of Goods Sold (COGS) calculation.
*   ❌ **`#3` Credit Aging Control (Accounts Receivable):** A report showing which customers have outstanding debts and for how long.
*   ❌ **`#4` Daily/Weekly Cash Flow Report:** A report focused on real cash inflows and outflows.
*   ❌ **`#11` Most Loyal Customers Card:** A dashboard card or report highlighting the most valuable customers.
*   ⏳ **`#5` Quick Add Product in Sales Form:** The ability to add a new product on-the-fly from the sales form.
*   ❌ **`#6` Barcode Scanner Integration:** Support for adding items via a device camera or USB barcode scanner.
*   ❌ **`#12` Restock Button in the Products Table:** A shortcut to initiate a restocking expense directly from the products list.

---

#### **PHASE 4: Finalization, Infrastructure & Deployment (Future Roadmap)**

*   ❌ **Comprehensive Testing:** Increase backend unit test coverage, implement integration tests with `Testcontainers`, and add UI/component tests on the frontend with `React Testing Library`.
*   ❌ **Full Containerization:** Create `Dockerfile`s and a `docker-compose.yml` to orchestrate all services (API, UI, PostgreSQL, etc.) with a single command.
*   ❌ **Database Migration Infrastructure:** Implement **Flyway** to manage database schema changes in a versioned and safe manner.
*   ❌ **CI/CD & Deployment:** Set up a pipeline (e.g., GitHub Actions) for automated builds, tests, and deployment to a cloud platform (e.g., Azure, AWS).
*   ⏳ **Final Documentation:** Refine this master document and create an impeccable `README.md` for the repository, complete with diagrams, GIFs, and clear instructions.
