package recrutec.recrutec.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import recrutec.recrutec.security.jwt.JwtAuthenticationEntryPoint;
import recrutec.recrutec.security.jwt.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração principal de segurança da aplicação.
 * 
 * Princípios SOLID aplicados:
 * - Single Responsibility: Responsável apenas pela configuração de segurança
 * - Open/Closed: Extensível para novas configurações sem modificar código existente
 * - Dependency Inversion: Depende de abstrações (interfaces do Spring Security)
 * 
 * Esta classe configura:
 * - Autenticação JWT stateless
 * - Autorização baseada em roles
 * - CORS para aplicações front-end
 * - Tratamento de exceções de segurança
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize e @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    /**
     * Configuração principal da cadeia de filtros de segurança
     * 
     * @param http Objeto HttpSecurity para configuração
     * @return SecurityFilterChain configurada
     * @throws Exception Em caso de erro na configuração
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilita CSRF pois usamos autenticação JWT stateless
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configura CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configura gerenciamento de sessão como stateless
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configura o ponto de entrada para erros de autenticação
                .exceptionHandling(exceptions -> 
                    exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                
                // Configuração de autorização de requisições
                .authorizeHttpRequests(auth -> auth
                    // Endpoints públicos - não requerem autenticação
                    .requestMatchers(getPublicEndpoints()).permitAll()
                    
                    // Endpoints administrativos - apenas ADMINs
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    
                    // Endpoints de recrutadores - ADMINs e RECRUTADOREs
                    .requestMatchers("/api/recrutador/**").hasAnyRole("ADMIN", "RECRUTADOR")
                    
                    // Endpoints de candidatos - todos os usuários autenticados
                    .requestMatchers("/api/candidato/**").hasAnyRole("ADMIN", "RECRUTADOR", "CANDIDATO")
                    
                    // Todas as outras requisições precisam de autenticação
                    .anyRequest().authenticated())
                
                // Adiciona o filtro JWT antes do filtro padrão de autenticação
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                
                .build();
    }

    /**
     * Define os endpoints que são públicos e não requerem autenticação
     * 
     * @return Array com os padrões de URLs públicas
     */
    private String[] getPublicEndpoints() {
        return new String[]{
                // Endpoints de autenticação
                "/api/auth/**",
                "/api/login",
                "/api/register",
                "/api/forgot-password",
                "/api/reset-password",
                
                // Endpoints públicos de vagas (visualização)
                "/api/public/vagas/**",
                
                // Documentação da API (Swagger/OpenAPI)
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**",
                
                // Recursos estáticos
                "/static/**",
                "/css/**",
                "/js/**",
                "/images/**",
                "/favicon.ico",
                
                // Páginas HTML estáticas (se necessário)
                "/",
                "/index.html",
                "/login.html",
                "/cadastrar.html",
                
                // Health check e actuator (se habilitado)
                "/actuator/health",
                "/actuator/info"
        };
    }

    /**
     * Configuração de CORS para permitir requisições de diferentes origens
     * 
     * @return CorsConfigurationSource configurada
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origens permitidas - ajustar conforme necessário para produção
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "https://localhost:*",
                "http://127.0.0.1:*"
                // Adicionar domínios de produção aqui
                // "https://meudominio.com"
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));
        
        // Permitir credenciais (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);
        
        // Headers expostos para o cliente
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count"
        ));
        
        // Tempo de cache para requisições preflight
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    /**
     * Bean para codificação de senhas usando BCrypt
     * 
     * @return PasswordEncoder configurado com BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Força 12 rounds para maior segurança
    }

    /**
     * Configuração do AuthenticationManager para autenticação de usuários
     * 
     * @return AuthenticationManager configurado
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return new ProviderManager(authProvider);
    }
}
