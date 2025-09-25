package recrutec.recrutec.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI para documentação automática da API
 *
 * Funcionalidades:
 * - Documentação automática de todos os endpoints
 * - Interface interativa para testar APIs
 * - Suporte a autenticação JWT
 * - Informações detalhadas sobre request/response
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(securityRequirement())
                .components(components());
    }

    /**
     * Informações gerais da API
     */
    private Info apiInfo() {
        return new Info()
                .title("RecruTEC API")
                .description("API REST para sistema de recrutamento e seleção.\n\n" +
                        "## Funcionalidades Principais\n" +
                        "- **Autenticação JWT**: Login seguro com tokens de acesso e refresh\n" +
                        "- **Gestão de Usuários**: Candidatos, Recrutadores e Administradores\n" +
                        "- **Gestão de Vagas**: CRUD completo com controle de acesso\n" +
                        "- **Inscrições**: Sistema de candidaturas em vagas\n\n" +
                        "## Roles de Usuário\n" +
                        "- **ADMIN**: Acesso total ao sistema\n" +
                        "- **RECRUTADOR**: Gerencia vagas e visualiza candidatos\n" +
                        "- **CANDIDATO**: Visualiza e se inscreve em vagas\n\n" +
                        "## Como Usar\n" +
                        "1. Registre-se usando `/api/auth/register/candidato` ou `/api/auth/register/recrutador`\n" +
                        "2. Faça login em `/api/auth/login` para obter o token JWT\n" +
                        "3. Use o token no botão 'Authorize' desta página\n" +
                        "4. Teste os endpoints conforme seu nível de acesso")
                .version("1.0.0")
                .contact(contact())
                .license(license());
    }

    /**
     * Informações de contato
     */
    private Contact contact() {
        return new Contact()
                .name("Equipe RecruTEC")
                .email("contato@recrutec.com")
                .url("https://github.com/recrutec/api");
    }

    /**
     * Informações de licença
     */
    private License license() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Configuração dos servidores
     */
    private List<Server> servers() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Servidor Local de Desenvolvimento");

        return List.of(localServer);
    }

    /**
     * Configuração de segurança JWT
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("Bearer Authentication");
    }

    /**
     * Componentes de segurança
     */
    private Components components() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication", securityScheme());
    }

    /**
     * Esquema de segurança JWT
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Token JWT obtido através do endpoint de login.\n\n" +
                        "**Formato:** Bearer {seu-token-jwt}\n\n" +
                        "**Exemplo:**\n" +
                        "```\n" +
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\n" +
                        "```\n\n" +
                        "**Como obter:**\n" +
                        "1. Faça login no endpoint `/api/auth/login`\n" +
                        "2. Copie o valor de `accessToken` da resposta\n" +
                        "3. Cole aqui no formato: Bearer {token}");
    }
}