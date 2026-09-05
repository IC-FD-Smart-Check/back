package org.fdsmartcheck.config;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.model.User;
import org.fdsmartcheck.model.enums.Role;
import org.fdsmartcheck.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Cria o administrador inicial na primeira subida, para o sistema não nascer
 * inacessível (não existe cadastro público).
 *
 * Roda apenas quando NÃO existe nenhum ADMIN no banco — reiniciar a aplicação
 * não recria o usuário nem redefine a senha de quem já existe.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminSeeder.class);

    private static final String PASSWORD_ALPHABET =
            "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int GENERATED_PASSWORD_LENGTH = 16;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.enabled:true}")
    private boolean enabled;

    @Value("${app.admin.name:Administrador}")
    private String name;

    @Value("${app.admin.email:admin@fdsmartcheck.local}")
    private String email;

    @Value("${app.admin.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            logger.warn("Não foi possível criar o admin inicial: o email {} já está em uso.", normalizedEmail);
            return;
        }

        boolean generated = password == null || password.isBlank();
        String rawPassword = generated ? generatePassword() : password;

        User admin = User.builder()
                .name(name)
                .email(normalizedEmail)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        userRepository.save(admin);

        if (generated) {
            // Só aparece nesta primeira subida; depois disso não há como recuperar
            logger.warn("""
                    
                    ===========================================================
                     ADMIN INICIAL CRIADO
                     email: {}
                     senha: {}
                     Anote agora e troque no primeiro acesso (tela Meu Perfil).
                     Para definir a senha você mesmo, use APP_ADMIN_PASSWORD.
                    ===========================================================""",
                    normalizedEmail, rawPassword);
        } else {
            logger.info("Admin inicial criado com o email {}", normalizedEmail);
        }
    }

    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(GENERATED_PASSWORD_LENGTH);

        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_ALPHABET.charAt(random.nextInt(PASSWORD_ALPHABET.length())));
        }

        return password.toString();
    }
}
