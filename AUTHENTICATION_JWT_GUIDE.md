# Estrutura de Autenticação JWT - RecruTec

## Visão Geral

Este documento descreve a implementação completa de autenticação JWT na aplicação RecruTec, seguindo os princípios SOLID e as melhores práticas de segurança.

## Componentes Implementados

### 1. Configuração de Segurança (`SecurityConfig.java`)
- **Função**: Configuração principal do Spring Security
- **Características**:
  - Autenticação stateless com JWT
  - Configuração CORS para aplicações front-end
  - Autorização baseada em roles (ADMIN, RECRUTADOR, CANDIDATO)
  - Endpoints públicos configurados
  - Tratamento de exceções de segurança

### 2. Provedor JWT (`JwtTokenProvider.java`)
- **Função**: Geração, validação e extração de informações dos tokens JWT
- **Características**:
  - Tokens de acesso e refresh separados
  - Validação de assinatura e expiração
  - Extração segura de claims
  - Configuração flexível de tempo de expiração

### 3. Filtro de Autenticação (`JwtAuthenticationFilter.java`)
- **Função**: Intercepta requisições HTTP e autentica usuários via JWT
- **Características**:
  - Extração automática do token do header Authorization
  - Validação e processamento de tokens
  - Criação do contexto de segurança do Spring
  - Tratamento robusto de erros

### 4. Ponto de Entrada (`JwtAuthenticationEntryPoint.java`)
- **Função**: Trata exceções de autenticação e retorna respostas padronizadas
- **Características**:
  - Respostas JSON estruturadas para erros
  - Log detalhado de tentativas de acesso não autorizado
  - Mensagens de erro personalizadas
  - Coleta de informações para auditoria

### 5. Serviço de Detalhes do Usuário (`CustomUserDetailsService.java`)
- **Função**: Carrega dados do usuário para autenticação
- **Características**:
  - Busca em múltiplas tabelas (Admin, Candidato, Recrutador)
  - Hierarquia de roles implementada
  - Integração transparente com Spring Security
  - Tratamento de usuários não encontrados

### 6. Serviço de Autenticação (`AuthenticationService.java`)
- **Função**: Gerencia processo de login, logout e renovação de tokens
- **Características**:
  - Autenticação segura com Spring Security
  - Geração de access e refresh tokens
  - Renovação automática de tokens
  - Invalidação de tokens (logout)

### 7. Controlador de Autenticação (`AuthController.java`)
- **Função**: Exposição de endpoints REST para autenticação
- **Características**:
  - Endpoints para login, refresh e logout
  - Validação de entrada com Bean Validation
  - Tratamento de exceções com respostas HTTP adequadas
  - Documentação de API completa

## Endpoints de Autenticação

### POST `/api/auth/login`
**Descrição**: Autentica um usuário e retorna tokens JWT

**Request Body**:
```json
{
  "email": "usuario@exemplo.com",
  "senha": "minhasenha123"
}
```

**Response (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "nome": "João Silva",
    "email": "usuario@exemplo.com",
    "role": "CANDIDATO"
  }
}
```

### POST `/api/auth/refresh`
**Descrição**: Renova o access token usando refresh token

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "nome": "João Silva",
    "email": "usuario@exemplo.com",
    "role": "CANDIDATO"
  }
}
```

### POST `/api/auth/logout`
**Descrição**: Invalida o refresh token (logout)

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Logout realizado com sucesso",
  "timestamp": 1640995200000
}
```

### GET `/api/auth/me`
**Descrição**: Verifica se o usuário está autenticado
- **Requer**: Header `Authorization: Bearer <token>`
- **Response**: Confirmação de autenticação

## Configuração de Roles e Permissões

### Hierarquia de Roles:
1. **ADMIN**: Acesso total à aplicação
2. **RECRUTADOR**: Pode gerenciar vagas e ver candidatos
3. **CANDIDATO**: Pode se candidatar a vagas

### Endpoints por Role:

#### Públicos (Sem autenticação):
- `POST /api/auth/**` - Endpoints de autenticação
- `GET /api/vagas/public/**` - Visualização pública de vagas
- Recursos estáticos (`/static/**`, `/css/**`, etc.)

#### ADMIN:
- Acesso total a todos os endpoints
- `DELETE /api/vagas/{id}` - Deletar qualquer vaga
- `POST /api/admin/**` - Endpoints administrativos

#### RECRUTADOR:
- `POST /api/vagas` - Criar vagas
- `PUT /api/vagas/{id}` - Editar suas próprias vagas
- `GET /api/vagas/{id}/candidatos` - Ver candidatos inscritos

#### CANDIDATO:
- `POST /api/vagas/{id}/inscrever` - Se inscrever em vagas
- `GET /api/vagas` - Listar vagas disponíveis

## Como Usar a Autenticação

### 1. No Frontend (JavaScript/TypeScript):

```javascript
// Login
const login = async (email, senha) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, senha })
  });
  
  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  }
  throw new Error('Login falhou');
};

// Fazer requisições autenticadas
const fazerRequisicaoAutenticada = async (url, options = {}) => {
  const token = localStorage.getItem('accessToken');
  
  return fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    }
  });
};

// Renovar token automaticamente
const renovarToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  
  const response = await fetch('/api/auth/refresh', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refreshToken })
  });
  
  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data;
  }
  
  // Token inválido, redirecionar para login
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  window.location.href = '/login.html';
};
```

### 2. Interceptor de Requisições (Axios):

```javascript
import axios from 'axios';

// Configurar interceptor de requisição
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Configurar interceptor de resposta para renovação automática
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        await renovarToken();
        const token = localStorage.getItem('accessToken');
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return axios(originalRequest);
      } catch (refreshError) {
        // Redirecionar para login
        window.location.href = '/login.html';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);
```

## Configurações Importantes

### 1. application.properties:
```properties
# Configurações JWT (já adicionadas)
jwt.secret=recrutecSecretKeyForJWTToken2024!@#$%^&*()_+
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

### 2. Dependências Maven (já adicionadas):
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`

## Melhorias Recomendadas

### 1. Implementar Blacklist de Tokens:
```java
// Usar Redis para armazenar tokens invalidados
@Service
public class TokenBlacklistService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public void blacklistToken(String token) {
        // Adicionar token à blacklist
        redisTemplate.opsForValue().set("blacklist:" + token, "true", 
            Duration.ofMillis(jwtTokenProvider.getAccessTokenExpiration()));
    }
    
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
```

### 2. Rate Limiting:
```java
// Implementar rate limiting para endpoints de login
@Component
public class LoginRateLimiter {
    
    private final Map<String, AtomicInteger> attempts = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String clientIp) {
        AtomicInteger count = attempts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
        return count.get() < MAX_ATTEMPTS;
    }
}
```

### 3. Auditoria de Segurança:
```java
// Log detalhado de eventos de segurança
@EventListener
public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
    // Log login bem-sucedido
}

@EventListener
public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
    // Log tentativa de login falhada
}
```

### 4. Validação de Senha Forte:
```java
// Validador customizado para senhas
@Component
public class PasswordValidator {
    
    public boolean isValidPassword(String password) {
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*\\d.*") &&
               password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }
}
```

## Testes

### 1. Teste de Unidade para JwtTokenProvider:
```java
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    
    @Test
    void shouldGenerateValidAccessToken() {
        // Implementar teste
    }
    
    @Test
    void shouldValidateTokenCorrectly() {
        // Implementar teste
    }
}
```

### 2. Teste de Integração para AuthController:
```java
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class AuthControllerIntegrationTest {
    
    @Test
    void shouldAuthenticateUserSuccessfully() {
        // Implementar teste de integração
    }
}
```

## Conclusão

A estrutura de autenticação JWT implementada fornece:
- **Segurança robusta** com tokens JWT assinados
- **Arquitetura modular** seguindo princípios SOLID
- **Flexibilidade** para diferentes tipos de usuário
- **Extensibilidade** para futuras funcionalidades
- **Monitoramento** através de logs detalhados

A implementação está pronta para uso em produção e pode ser facilmente estendida conforme necessário.
