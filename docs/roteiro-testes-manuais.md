# Roteiro de Testes Manuais - SGVS API

## 1. Objetivo
Validar manualmente todos os fluxos expostos pela API (`/api/**`) para garantir que regras de negócio, segurança e integrações entre módulos estão funcionando ponta a ponta.

## 2. Escopo Coberto (código atual)
- Autenticação e perfil de usuário
- Categorias
- Fornecedores
- Produtos
- Clientes
- Vendas
- Pagamentos
- Despesas
- Configurações gerais
- Relatórios
- Dashboard
- Segurança (JWT, autorização e isolamento por usuário)

## 3. Pré-requisitos
1. Banco PostgreSQL disponível e configurado em `application.properties`.
2. Variáveis exportadas:
   - `DB_PASSWORD`
   - `JWT_SECRET_KEY` (Base64 válido para HS256)
3. API em execução:
   - `./mvnw spring-boot:run`
4. Cliente para testes manuais (Postman/Insomnia/curl).
5. Coleção com variável `{{baseUrl}}=http://localhost:8080`.

## 4. Convenções de Teste
- Headers padrão para rotas protegidas:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- Datas com timezone ISO-8601, ex. `2026-03-02T10:00:00Z`.
- Validar sempre:
  - HTTP status
  - Corpo da resposta
  - Efeito colateral em endpoints relacionados

## 5. Massa Base Recomendada
Criar durante os testes:
- Usuário A e Usuário B
- 2 categorias (ex.: Bebidas, Mercearia)
- 2 fornecedores
- 4 produtos com combinações de `active`, `managesStock`, `minimumStock`
- 3 clientes (1 com crédito habilitado e limite alto, 1 sem crédito, 1 para dívida)

## 6. Matriz de Fluxos e Casos

## 6.1 Autenticação e Usuário
### A01 - Registrar usuário com sucesso
- Endpoint: `POST /api/auth/register`
- Payload válido (username/email/password)
- Esperado: `201`, retorno com `token`, `id`, `username`, `email`, `role=USER`.

### A02 - Registrar com username duplicado
- Repetir `username` do A01
- Esperado: `409`.

### A03 - Registrar com email inválido
- Esperado: `422` (validação).

### A04 - Login com username
- Endpoint: `POST /api/auth/login`
- Esperado: `200` com `token`.

### A05 - Login com email
- Esperado: `200`.

### A06 - Login com senha inválida
- Esperado: `401`.

### A07 - Bloqueio por tentativas de login
- Realizar 5+ tentativas inválidas em até 15 min
- Esperado: após limite, `429`.

### A08 - Acesso sem token em rota protegida
- Ex.: `GET /api/products`
- Esperado: `403`/`401` (negado).

### A09 - Perfil do usuário autenticado
- Endpoint: `GET /api/users/me`
- Esperado: `200` com dados do usuário logado.

### A10 - Atualizar perfil
- Endpoint: `PUT /api/users/me`
- Trocar username/email/senha
- Esperado: `204`; novo login com senha nova funciona.

### A11 - Deletar conta
- Endpoint: `DELETE /api/users/me`
- Esperado: `204`; login subsequente falha (`401`).

## 6.2 Categorias
### C01 - Criar categoria
- `POST /api/categories`
- Esperado: `201`.

### C02 - Listar categorias
- `GET /api/categories`
- Esperado: `200` com item criado.

### C03 - Atualizar categoria
- `PUT /api/categories/{id}`
- Esperado: `200`.

### C04 - Deletar categoria sem vínculo
- `DELETE /api/categories/{id}`
- Esperado: `204`.

### C05 - Deletar categoria com vínculos
- Categoria usada por produto/venda
- Esperado: `422` com mensagem de bloqueio.

## 6.3 Fornecedores
### F01 - Criar fornecedor
- `POST /api/providers`
- Esperado: `201`.

### F02 - Listar fornecedores
- `GET /api/providers`
- Esperado: `200`.

## 6.4 Produtos
### P01 - Criar produto válido
- `POST /api/products`
- Incluindo `categoryId`, `salePrice`, `stockQuantity`, `unitOfSale`
- Esperado: `201`.

### P02 - Criar com `categoryId` inexistente
- Esperado: `404`.

### P03 - Listagem paginada e filtros
- `GET /api/products?page=0&size=10&name=...&categoryId=...`
- Esperado: `200` + `content/totalElements/totalPages`.

### P04 - Ordenações
- Testar: `name_asc`, `name_desc`, `price_asc`, `price_desc`, `date_asc`, `date_desc`, `mostSold`, `leastSold`
- Esperado: ordenação coerente.

### P05 - Buscar por ID
- `GET /api/products/{id}`
- Esperado: `200`.

### P06 - Suggestions
- `GET /api/products/suggestions`
- Esperado: até 3 itens relevantes.

### P07 - Atualizar produto
- `PUT /api/products/{id}`
- Esperado: `200`.

### P08 - Copiar produto
- `POST /api/products/{id}/copy`
- Esperado: `201`, nome com sufixo `- Copy (1)`, `active=false`, `stockQuantity=0`.

### P09 - Alternar status ativo/inativo
- `PATCH /api/products/{id}/status`
- Esperado: `204`; status invertido.

### P10 - Excluir produto permanentemente
- `DELETE /api/products/{id}/permanent`
- Esperado: `204`.

### P11 - Calcular preço sugerido
- `GET /api/products/calculate-price?costPrice=100&desiredProfitMargin=20`
- Esperado: `200`, valor `120.00`.

### P12 - Low stock
- `GET /api/products/low-stock`
- Esperado: produtos com `stockQuantity < minimumStock`.

## 6.5 Clientes
### CL01 - Criar cliente
- `POST /api/customers`
- Esperado: `201`.

### CL02 - Duplicidade de taxId
- Mesmo `taxId` no mesmo usuário
- Esperado: `409`.

### CL03 - Listar com filtros
- `GET /api/customers?name=...&isActive=true&hasDebt=false&orderBy=name_asc`
- Esperado: `200`.

### CL04 - Buscar por ID
- `GET /api/customers/{id}`
- Esperado: `200`.

### CL05 - Suggestions
- `GET /api/customers/suggestions`
- Esperado: até 3 clientes.

### CL06 - Atualizar cliente
- `PUT /api/customers/{id}`
- Esperado: `200`.

### CL07 - Alterar status para inativo sem dívida
- `PATCH /api/customers/{id}/status` com `{"active":false}`
- Esperado: `204`.

### CL08 - Bloquear inativação com dívida
- Cliente com `debtBalance > 0`
- Esperado: `422`.

### CL09 - Excluir cliente
- `DELETE /api/customers/{id}`
- Esperado: `204`.

## 6.6 Configurações de Estoque
### GS01 - Ler configurações
- `GET /api/settings`
- Esperado: `200`; se não existir, sistema cria padrão `PER_ITEM`.

### GS02 - Atualizar `stockControlType`
- `PUT /api/settings` com `NONE`, `PER_ITEM`, `GLOBAL`
- Esperado: `200`.

## 6.7 Vendas
### V01 - Venda à vista sem cliente
- `POST /api/sales` com `paymentMethod=CASH`
- Esperado: `201`, `paymentStatus=NOT_APPLICABLE`.

### V02 - Venda a prazo sem cliente
- `paymentMethod=ON_CREDIT` sem `customerId`
- Esperado: `422`.

### V03 - Venda a prazo com cliente sem crédito habilitado
- Esperado: `422`.

### V04 - Venda a prazo excedendo limite
- Esperado: `422`.

### V05 - Venda a prazo válida
- Esperado: `201`, `paymentStatus=PENDING`, dívida do cliente incrementada.

### V06 - Regras de estoque por configuração
- Com `GLOBAL`: toda venda decrementa estoque
- Com `PER_ITEM`: decrementa só produto com `managesStock=true`
- Com `NONE`: não decrementa
- Esperado: comportamento conforme modo.

### V07 - Bloqueio por estoque insuficiente
- Esperado: `422`.

### V08 - Listagem e filtros
- `GET /api/sales` com filtros: data, cliente, método, status, produto, paginação, `orderBy`
- Esperado: `200`.

### V09 - Total bruto
- `GET /api/sales/gross-total` com/sem filtros
- Esperado: `200` com valor coerente.

### V10 - Total por método de pagamento
- `GET /api/sales/total-by-payment-method`
- Esperado: `200`, agrupamento correto.

### V11 - Resumo por grupo
- `GET /api/sales/summary-by-group?groupBy=day|customer|paymentMethod`
- Esperado: `200`.

### V12 - Resumo por grupo inválido
- `groupBy=invalid`
- Esperado: `422`.

### V13 - Exclusão permanente de venda
- `DELETE /api/sales/{id}/permanent`
- Esperado: `204`; estoque retorna (se produto gerencia estoque) e dívida de venda a prazo é revertida.

## 6.8 Pagamentos
### PG01 - Quitar vendas pendentes corretamente
- `POST /api/payments` com `customerId`, `saleIds`, `paymentMethod != ON_CREDIT`, `amountPaid` igual ao total
- Esperado: `204`; vendas mudam para `PAID`; dívida do cliente reduz.

### PG02 - Método de pagamento ON_CREDIT no pagamento
- Esperado: `422`.

### PG03 - Valor pago divergente do total
- Esperado: `422`.

### PG04 - Venda de outro cliente
- Esperado: `422`.

### PG05 - Venda não pendente
- Esperado: `422`.

## 6.9 Despesas
### D01 - Criar despesa simples
- `POST /api/expenses` com `expenseType != RESTOCKING` e `value > 0`
- Esperado: `201`.

### D02 - Despesa simples sem valor
- Esperado: `422`.

### D03 - Despesa simples com `restockItems`
- Esperado: `422`.

### D04 - Criar despesa de reposição
- `expenseType=RESTOCKING` com `restockItems`
- Esperado: `201`; `value` calculado automaticamente; estoque dos produtos aumenta.

### D05 - Reposição sem itens
- Esperado: `422`.

### D06 - Listagem paginada e filtros
- `GET /api/expenses?name=...&expenseType=...&startDate=...&endDate=...`
- Esperado: `200`.

### D07 - Total de despesas
- `GET /api/expenses/total`
- Esperado: `200` com soma correta.

### D08 - Buscar por ID
- `GET /api/expenses/{id}`
- Esperado: `200`.

### D09 - Atualizar despesa
- `PUT /api/expenses/{id}`
- Esperado: `200`.

### D10 - Excluir despesa
- `DELETE /api/expenses/{id}`
- Esperado: `204`.

## 6.10 Relatórios e Dashboard
### R01 - Resumo financeiro
- `GET /api/reports/financial-summary?startDate=...&endDate=...`
- Esperado: `200`; margens e lucros coerentes com vendas/despesas.

### R02 - Curva ABC
- `GET /api/reports/abc-analysis?startDate=...&endDate=...`
- Esperado: `200`; classes A/B/C retornadas.

### DB01 - Dashboard summary
- `GET /api/dashboard/summary?startDate=...&endDate=...`
- Esperado: `200`; cards e séries preenchidos.

## 6.11 Segurança e Isolamento Multiusuário
### S01 - Isolamento por usuário em listagens
- Usuário A cria dados; Usuário B lista
- Esperado: B não enxerga dados de A.

### S02 - Acesso cruzado por ID
- Usuário B tenta `GET/PUT/DELETE` em ID de A
- Esperado: `404`/erro de negócio, nunca sucesso.

### S03 - Token inválido/expirado
- Esperado: `401`.

### S04 - CORS
- Chamada com origem permitida e não permitida
- Esperado: origem fora de `app.cors.allowed-origins` bloqueada no browser.

## 7. Sequência Recomendada de Execução (E2E)
1. Auth: registrar, logar, capturar token.
2. Cadastros base: categoria, fornecedor, produtos, clientes.
3. Configurações: alternar modos de controle de estoque.
4. Vendas: cenários à vista e a prazo.
5. Pagamentos: quitação de vendas pendentes.
6. Despesas: simples e reposição.
7. Relatórios e dashboard: validar agregações.
8. Exclusões e reversões: venda, produto, cliente, categoria.
9. Segurança/multiusuário: repetir com Usuário B.

## 8. Critério de Saída
- 100% dos casos acima executados.
- Nenhum erro 5xx.
- Todas as regras críticas confirmadas:
  - crédito e limite
  - estoque por modo (`NONE/PER_ITEM/GLOBAL`)
  - pagamentos e reconciliação de dívida
  - isolamento por usuário autenticado

## 9. Riscos/atenções para observar durante execução
- Endpoint de cálculo de preço retorna `null` se faltar parâmetro; validar comportamento esperado no cliente.
- Em exclusão de venda, o reestoque verifica `managesStock` do produto, não o `stockControlType` global vigente no momento da exclusão.
- Atualização de despesa de reposição não recalcula automaticamente estoque (somente criação faz ajuste explícito).
