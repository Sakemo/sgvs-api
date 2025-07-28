### **Documento Mestre do Projeto: Sistema de Gestão de Vendas Simplificado (SGVS)**

**Versão:** 1.0 (Pós-Migração)
**Última Atualização:** 28 de Julho de 2025

#### **1. Introdução e Visão do Projeto**

O **Sistema de Gestão de Vendas Simplificado (SGVS)** é uma aplicação web full-stack projetada para ser uma ferramenta de gestão poderosa, intuitiva e visualmente impactante para pequenos e médios negócios. A filosofia central do projeto é ir além de um simples registro de transações, oferecendo ao usuário uma experiência de "CEO", com insights acionáveis e controle total sobre as operações financeiras e de inventário. O software visa transformar dados brutos em inteligência de negócio, com foco em fluxo de caixa, lucratividade e performance de produtos.

Construído sobre uma arquitetura moderna e escalável, o SGVS utiliza React (Vite + TypeScript) no frontend e Java (Spring Boot) no backend, com PostgreSQL para dados relacionais. A aplicação foi desenvolvida com os princípios de "Nível MIT": código limpo, arquitetura desacoplada, testes robustos, e uma experiência de usuário (UX) excepcional, incluindo internacionalização (i18n), tema dark/light, e componentes de UI reativos. Este documento serve como um roteiro mestre para o desenvolvimento contínuo, detalhando o que foi concluído e quais são os próximos passos estratégicos.

---

### **2. Roadmap Mestre do Projeto**

✅ = Concluído | ⏳ = Em Andamento / Parcialmente Concluído | 🎯 = Próximo Alvo | ❌ = Não Iniciado

#### **FASE 1: Fundação e Migração da Arquitetura (100% ✅)**

Esta fase focou em recriar a aplicação do zero, estabelecendo uma base técnica de alta qualidade.

*   ✅ **Backend:** Projeto Spring Boot com arquitetura limpa, 100% em inglês (pacotes, classes, métodos).
*   ✅ **Frontend:** Projeto React/Vite com arquitetura moderna, TypeScript, e componentes de UI reutilizáveis baseados em `CVA`.
*   ✅ **Banco de Dados:** Schema PostgreSQL 100% em inglês, gerenciado via `ddl-auto` em desenvolvimento.
*   ✅ **Features Essenciais Migradas:**
    *   **Produtos:** CRUD completo, filtros, ordenação (incluindo "mais vendido"), cópia, painel de detalhes.
    *   **Clientes:** CRUD completo, filtros, ordenação, painel de detalhes.
    *   **Despesas:** CRUD completo, filtros, painel de detalhes, com lógica implementada para **Despesas de Reabastecimento** que atualizam o estoque.
    *   **Vendas:** CRUD completo, filtros, paginação, cards de totais (bruto, líquido), autocomplete inteligente para produtos e clientes.
    *   **Configurações:** Implementação do **Controle de Estoque Configurável** (GLOBAL, PER_ITEM, NONE).
    *   **Dashboard:** Todos os cards e gráficos planejados estão funcionais e conectados à API.
*   ✅ **UX "Nível MIT":**
    *   `i18n` completo (inglês/português).
    *   Sistema de Notificação Global (Toasts) com `react-hot-toast`.
    *   Modal de Confirmação Global para ações críticas.
    *   Componente `AdvancedOptions` para simplificar formulários complexos.
    *   Tema Dark/Light funcional em toda a aplicação.
*   ✅ **Qualidade de Código:** Testes unitários abrangentes para a camada de serviço do backend (`Product`, `Sale`, `Customer`, `Expense`).

---

#### **🎯 FASE 2: Autenticação e Multi-usuário (Próximo Alvo)**

Esta é a próxima grande etapa para transformar a aplicação em um produto seguro e pronto para múltiplos usuários.

*   🎯 **Backend - Spring Security & JWT:**
    *   **O quê:** Proteger toda a API.
    *   **Como:**
        1.  Introduzir a entidade `User` (`username`, `password`, `roles`).
        2.  Configurar Spring Security para usar **JWT (JSON Web Tokens)** para autenticação stateless.
        3.  Criar endpoints de autenticação: `POST /api/auth/register` e `POST /api/auth/login`.
        4.  Implementar um filtro JWT que valide o token em cada requisição para endpoints protegidos.
*   🎯 **Frontend - Fluxo de Login e Rotas Protegidas:**
    *   **O quê:** Criar a experiência de login e proteger o acesso às páginas.
    *   **Como:**
        1.  Criar uma `LoginPage` e uma `RegisterPage`.
        2.  Desenvolver um **Contexto de Autenticação (`AuthContext`)** para gerenciar o estado do usuário (logado/deslogado) e o token JWT em toda a aplicação.
        3.  Implementar um componente `ProtectedRoute` que envolverá as rotas no `App.tsx`, redirecionando usuários não autenticados.
        4.  Atualizar o `apiClient` (interceptor de requisição) para adicionar o `Authorization: Bearer <token>` em todas as chamadas à API.

---

#### **FASE 3: Inteligência de Negócio e "Magia" para o Usuário (20% ⏳)**

Esta fase foca em transformar os dados em insights ainda mais valiosos e em aprimorar o fluxo de trabalho do usuário.

*   ✅ **`#8` Profit Margin in Product Table:** Concluído.
*   ✅ **`#2` Product ABC Curve Report:** Concluído.
*   ⏳ **`#1` Low Stock Alert:** Um sistema de notificação para produtos com baixo estoque.
*   ⏳ **`#9` Suggested Selling Price:** Sugestão de preço de venda baseada no custo e na margem de lucro desejada.
*   ❌ **`#10` Loss Management System:** Ferramenta para registrar perdas de estoque (produtos danificados, expirados), crucial para o cálculo preciso do Custo dos Bens Vendidos (COGS).
*   ❌ **`#3` Credit Aging Control (Contas a Receber):** Relatório de envelhecimento de dívidas de clientes.
*   ❌ **`#4` Daily/Weekly Cash Flow Report:** Relatório focado em entradas e saídas de caixa.
*   ❌ **`#11` Most Loyal Customers Card:** Destaque para os clientes mais valiosos no dashboard.
*   ⏳ **`#5` Quick Add Product in Sales Form:** Adicionar um produto "on-the-fly" a partir do formulário de vendas.
*   ❌ **`#6` Barcode Scanner Integration:** Suporte para adicionar itens via scanner de código de barras.
*   ❌ **`#12` Restock Button in the Products Table:** Atalho para iniciar um reabastecimento a partir da lista de produtos.

---

#### **FASE 4: Finalização, Infraestrutura e Deploy (Roteiro Futuro)**

*   ❌ **Testes Abrangentes:** Aumentar a cobertura de testes no backend, implementar testes de integração com `Testcontainers`, e adicionar testes de UI/componentes no frontend com `React Testing Library`.
*   ❌ **Containerização Completa:** Criar `Dockerfile`s e um `docker-compose.yml` para orquestrar todos os serviços (API, UI, PostgreSQL, etc.) com um único comando.
*   ❌ **Infraestrutura de Migração de Banco:** Implementar **Flyway** para gerenciar as mudanças no schema do banco de dados de forma versionada e segura.
*   ❌ **CI/CD e Deploy:** Configurar um pipeline (ex: GitHub Actions) para automação de build, teste e deploy em uma plataforma de nuvem (ex: Azure, AWS).
*   ⏳ **Documentação Final:** Refinar este documento e criar um `README.md` impecável para o repositório.
