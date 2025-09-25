package recrutec.recrutec.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import recrutec.recrutec.model.User;
import recrutec.recrutec.service.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Implementação simplificada do UserDetailsService usando a entidade User única.
 *
 * Integra perfeitamente com Spring Security usando Role enum para autorização.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Carregando detalhes do usuário: {}", email);

        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isEmpty()) {
            log.warn("Usuário não encontrado: {}", email);
            throw new UsernameNotFoundException("Usuário não encontrado: " + email);
        }

        User user = userOptional.get();
        log.debug("Usuário encontrado: {} ({})", email, user.getRole());

        return new CustomUserPrincipal(user);
    }

    /**
     * Implementação personalizada do UserDetails que encapsula nossa entidade User
     */
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Converte Role enum para GrantedAuthority com prefixo ROLE_
            return Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

        @Override
        public String getPassword() {
            return user.getSenha();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true; // Poderia implementar lógica de expiração se necessário
        }

        @Override
        public boolean isAccountNonLocked() {
            return true; // Poderia implementar lógica de bloqueio se necessário
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true; // Poderia implementar lógica de expiração de credenciais
        }

        @Override
        public boolean isEnabled() {
            return true; // Poderia implementar campo 'enabled' na entidade User
        }

        // Métodos de conveniência para acessar dados do usuário
        public User getUser() {
            return user;
        }

        public Long getId() {
            return user.getId();
        }

        public String getNome() {
            return user.getNome();
        }

        public String getRole() {
            return user.getRole().name();
        }
    }
}
