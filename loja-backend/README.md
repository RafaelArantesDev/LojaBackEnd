# Loja Virtual — Backend (Spring Boot + MySQL)

API REST em Java 21 / Spring Boot / Spring Data JPA / Maven.
Camadas: `model` (Entity) → `repository` → `service` → `controller`, mais `dto`, `security` e `config`.

## Endpoints de Categorias (CRUD)

| Método | Endpoint | Função | Sucesso |
|---|---|---|---|
| GET | `/api/categorias` | Listar todas | 200 |
| GET | `/api/categorias/{id}` | Buscar por ID | 200 / 404 |
| POST | `/api/categorias` | Cadastrar (`{"nome":"Games"}`) | 201 |
| PUT | `/api/categorias/{id}` | Atualizar | 200 / 404 |
| DELETE | `/api/categorias/{id}` | Excluir | 204 / 404 |

Regras: nome obrigatório (máx. 45), sem nomes repetidos (409) e não exclui categoria que ainda tem produtos (409).
Erros voltam como `{"mensagem": "..."}`.

Outras rotas: `GET /api/teste` e o CRUD de `/api/produtos`.

### Login (Supabase)
O login é feito no React com o Supabase. O backend tem um modo opcional de proteção:
- `APP_AUTH_ATIVA=false` (padrão): a API fica aberta, como nas aulas — o professor consegue testar os
  endpoints no Postman pela URL pública sem token.
- `APP_AUTH_ATIVA=true`: toda rota `/api/**` (exceto `/api/teste`) exige `Authorization: Bearer <token do Supabase>`.
  O backend confere o token no próprio Supabase (`SUPABASE_URL` + `SUPABASE_ANON_KEY`).

## 1. Rodar localmente
1. Importe `banco/loja_gestao_hospedagem.sql` num MySQL local, em um banco chamado `gestao`.
2. Defina a variável `DB_PASSWORD` com a senha do seu MySQL e execute `./mvnw spring-boot:run`
   (ou rode `LojaApplication` no IntelliJ).
3. Teste: abra http://localhost:8080/api/teste (deve mostrar "API funcionando corretamente") e http://localhost:8080/api/categorias.

## 2. Banco no Aiven
1. No Aiven, crie um serviço **MySQL** e aguarde ficar *Running*.
2. Na página do serviço anote **Host, Port, User, Password** e o nome do banco (normalmente `defaultdb`).
3. Importe os dados nesse banco, por exemplo:
   ```bash
   mysql --host HOST --port PORTA --user avnadmin --password --ssl-mode=REQUIRED defaultdb < banco/loja_gestao_hospedagem.sql
   ```
   (ou MySQL Workbench: conecte com SSL e rode *Server > Data Import* escolhendo `defaultdb` como destino).
   Se aparecer erro sobre tabela sem chave primária, confirme que a linha `SET SESSION sql_require_primary_key = 0;`
   do início do arquivo foi executada; se o seu usuário não tiver permissão, desative
   `sql_require_primary_key` nas configurações avançadas do serviço no Aiven e importe de novo.
4. Se o serviço pedir liberação de IP, permita acesso de qualquer origem (o Render usa IPs variáveis).
5. Monte a URL JDBC:
   `jdbc:mysql://HOST:PORTA/defaultdb?sslMode=REQUIRED&serverTimezone=UTC`

## 3. Backend no Render
1. Suba esta pasta para um repositório Git (GitHub).
2. No Render: **New > Web Service** → escolha o repositório → **Language/Runtime: Docker**.
3. **Health Check Path:** `/api/teste`
4. Em **Environment**, crie:

| Variável | Valor |
|---|---|
| `DB_URL` | a URL JDBC do passo anterior |
| `DB_USERNAME` | usuário do Aiven (ex.: `avnadmin`) |
| `DB_PASSWORD` | senha do Aiven |
| `CORS_ORIGINS` | `http://localhost:5173` (front local). Vários: separe por vírgula |
| `APP_AUTH_ATIVA` | *(opcional)* `true` para exigir login do Supabase na API; omita ou `false` para API aberta |
| `SUPABASE_URL` / `SUPABASE_ANON_KEY` | *(só se `APP_AUTH_ATIVA=true`)* URL do projeto e chave pública do Supabase |

5. Após o deploy, teste `https://SEU-SERVICO.onrender.com/api/teste` (deve responder "API funcionando corretamente").
6. Para testar o CRUD pela URL pública, use o Postman em `https://SEU-SERVICO.onrender.com/api/categorias`
   (com `APP_AUTH_ATIVA=true`, inclua o header `Authorization: Bearer <token do Supabase>`).

Obs.: no plano gratuito o Render "adormece" o serviço; a primeira chamada pode demorar cerca de 1 minuto.
