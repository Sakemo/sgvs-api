package com.flick.business.service.security;

import com.flick.business.api.dto.auth.AuthResponse;
import com.flick.business.api.dto.auth.LoginRequest;
import com.flick.business.api.dto.auth.RegisterRequest;
import com.flick.business.api.dto.auth.GoogleTokenPayload;
import com.flick.business.core.entity.GeneralSettings;
import com.flick.business.core.entity.security.User;
import com.flick.business.core.enums.security.Role;
import com.flick.business.core.enums.settings.StockControlType;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.LoginAttemptsExceededException;
import com.flick.business.exception.ResourceAlreadyExistsException;
import com.flick.business.exception.InvalidTokenException;
import com.flick.business.repository.GeneralSettingsRepository;
import com.flick.business.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cglib.core.Local;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final ConcurrentMap<String, LoginAttemptState> LOGIN_ATTEMPTS = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final GeneralSettingsRepository generalSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionRegistryService sessionRegistryService;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifier googleTokenVerifier;

    /**
     * Registers a new user in the system.
     *
     * @param request The registration request containing username and password.
     * @return An AuthResponse containing the JWT for the newly created user.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String rawPassword = request.getPassword();

        validateNormalizedRegistrationInput(username, email, rawPassword);

        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("A user with this username already exists.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("A user with this email already exists.");
        }

        var user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.USER)
                .build();

        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceAlreadyExistsException("Username or email already in use.");
        }

        ensureDefaultSettings(savedUser);

        String sessionId = sessionRegistryService.rotateSession(savedUser.getId());
        var jwtToken = jwtService.generateToken(Map.of("sid", sessionId), savedUser);

        return AuthResponse.builder()
                .token(jwtToken)
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(resolveEffectiveEmail(savedUser))
                .role(savedUser.getRole())
                .build();
    }

    /**
     * Authenticates an existing user.
     *
     * @param request The login request containing username and password.
     * @return An AuthResponse containing the JWT upon successful authentication.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String usernameOrEmail = request.getUsername().trim();
        String password = request.getPassword();
        validateNormalizedLoginInput(usernameOrEmail, password);
        checkRateLimit(usernameOrEmail);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            usernameOrEmail,
                            password));
        } catch (AuthenticationException ex) {
            registerFailedAttempt(usernameOrEmail);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        var user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));
        applyLegacyIdentityFallbacks(user, usernameOrEmail);

        resetFailedAttempts(usernameOrEmail);
        String sessionId = sessionRegistryService.rotateSession(user.getId());
        var jwtToken = jwtService.generateToken(Map.of("sid", sessionId), user);

        return AuthResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .username(resolveEffectiveUsername(user))
                .email(resolveEffectiveEmail(user))
                .role(user.getRole())
                .build();
    }

    /**
     * Autentica um usuário usando ID token do Google (OAuth 2.0)
     *
     * @param idToken O ID token fornecido pelo Google
     * @return Um AuthResponse contendo o JWT para o usuário
     * @throws InvalidTokenException Se o token for inválido ou expirado
     * @throws LoginAttemptsExceededException Se o usuário exceder o limite de tentativas
     */
    @Transactional
    public AuthResponse googleLogin(String idToken) {
        // 1. Validar token com Google
        GoogleTokenPayload tokenPayload = googleTokenVerifier.verifyToken(idToken);
        String email = tokenPayload.getEmail().toLowerCase();

        // 2. Aplicar rate limiting baseado no email
        checkRateLimit(email);

        try {
            // 3. Procurar usuário existente por email
            User user = userRepository.findByUsernameOrEmail(email, email)
                    .orElseGet(() -> createGoogleUser(tokenPayload));

            // 4. Atualizar dados do Google se não existiam
            boolean userUpdated = false;
            if (user.getGoogleId() == null) {
                user.setGoogleId(tokenPayload.getGoogleId());
                userUpdated = true;
            }
            if (user.getGoogleProfilePicture() == null && tokenPayload.getPicture() != null) {
                user.setGoogleProfilePicture(tokenPayload.getPicture());
                userUpdated = true;
            }
            user.setGoogleLoginDate(LocalDateTime.now());
            userUpdated = true;

            if (userUpdated) {
                userRepository.save(user);
            }

            // 5. Limpar tentativas falhadas
            resetFailedAttempts(email);

            // 6. Gerar JWT
            String sessionId = sessionRegistryService.rotateSession(user.getId());
            var jwtToken = jwtService.generateToken(Map.of("sid", sessionId), user);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .id(user.getId())
                    .username(resolveEffectiveUsername(user))
                    .email(resolveEffectiveEmail(user))
                    .role(user.getRole())
                    .build();

        } catch (Exception e) {
            if (e instanceof InvalidTokenException || e instanceof LoginAttemptsExceededException) {
                throw e;
            }
            // Registrar tentativa falhada para rate limiting
            registerFailedAttempt(email);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Erro ao processar autenticação com Google");
        }
    }

    /**
     * Cria um novo usuário a partir dos dados do Google
     */
    private User createGoogleUser(GoogleTokenPayload tokenPayload) {
        String email = tokenPayload.getEmail().toLowerCase();
        
        // Gerar username a partir do email ou name
        String username = tokenPayload.getName() != null && !tokenPayload.getName().isBlank() 
            ? normalizeUsername(tokenPayload.getName())
            : normalizeUsername(email.substring(0, email.indexOf('@')));

        // Garantir unicidade do username
        String finalUsername = username;
        int counter = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = username + counter;
            counter++;
        }

        var user = User.builder()
                .username(finalUsername)
                .email(email)
                .password(passwordEncoder.encode(generateRandomPassword())) // Sem password real para usuários Google
                .role(Role.USER)
                .googleId(tokenPayload.getGoogleId())
                .googleProfilePicture(tokenPayload.getPicture())
                .googleLoginDate(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        ensureDefaultSettings(savedUser);

        return savedUser;
    }

    /**
     * Normaliza um nome para username (remove espaços, converte para lowercase)
     */
    private String normalizeUsername(String input) {
        if (input == null || input.isBlank()) {
            return "user";
        }
        return input.toLowerCase()
                .replaceAll("[^a-z0-9._]", "")
                .replaceAll("^\\.+|\\.+$", "") // Remove pontos no início/fim
                .replaceAll("\\.{2,}", ".") // Remove múltiplos pontos
                .replaceAll("_{2,}", "_"); // Remove múltiplos underscores
    }

    /**
     * Gera uma senha aleatória para usuários criados via Google (nunca será usada)
     */
    private String generateRandomPassword() {
        return java.util.UUID.randomUUID().toString() + java.util.UUID.randomUUID().toString();
    }

    // Normalize E-mail
    public String normalizeEmail(String email){
        if(email == null || email.trim().isEmpty()){
            return "Invalid Email";
        }

        email = email.toLowerCase().trim();
        String[] splits = email.split("@");
        if(splits.length != 2){
            return email;
        }

        String localPart = splits[0];
        String domain = splits[1];

        if(domain == "gmail.com"){
            localPart = localPart.replace(".", "");

            int plusIndex = localPart.indexOf("+");
            if (plusIndex >= 0){
                localPart = localPart.substring(plusIndex);
            }
        }

        String normalizedEmail = localPart + "@" + domain;
        return normalizedEmail;
    }

    // Create Code
    public String createCode(int size){
        SecureRandom random = new SecureRandom();

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codeBuilder = new StringBuilder(size);

        for(int i=0; i<size; i++){
            int index = random.nextInt(characters.length());
            codeBuilder.append(characters.charAt(index));
        }

        String code = codeBuilder.toString();
        return code;
    }

    public void requestPasswordReset(String email) {
        LocalDateTime request = LocalDateTime.now();
        LocalDateTime expiration = request.plusMinutes(10);

        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        if (user == null) {
            throw new BusinessException("User not found with the provided email.");
        }
        String code = createCode(6);

        user.setPasswordResetCode(code);
        user.setPasswordResetRequestedAt(request);
        user.setPasswordResetCodeExpiresAt(expiration);
        user.setPasswordResetAttempts(0);
        userRepository.save(user);

        // envia o código por email
    }

    private void validateNormalizedRegistrationInput(String username, String email, String password) {
        if (username.isBlank() || username.length() < 3 || username.length() > 50) {
            throw new BusinessException("Username must have between 3 and 50 characters.");
        }

        if (email.isBlank() || !SIMPLE_EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("Email must be valid.");
        }

        if (password == null || password.length() < 8 || password.length() > 100) {
            throw new BusinessException("Password must have between 8 and 100 characters.");
        }
    }

    private void validateNormalizedLoginInput(String usernameOrEmail, String password) {
        if (usernameOrEmail.isBlank() || usernameOrEmail.length() < 3 || usernameOrEmail.length() > 100) {
            throw new BusinessException("Username or email must have between 3 and 100 characters.");
        }
        if (password == null || password.isBlank() || password.length() > 100) {
            throw new BusinessException("Password must have at most 100 characters.");
        }
    }

    private void checkRateLimit(String usernameOrEmail) {
        LoginAttemptState state = LOGIN_ATTEMPTS.get(usernameOrEmail);
        if (state == null) {
            return;
        }
        Instant now = Instant.now();
        if (state.lockedUntil != null && now.isBefore(state.lockedUntil)) {
            throw new LoginAttemptsExceededException(state.lockedUntil);
        }
    }

    private void registerFailedAttempt(String usernameOrEmail) {
        Instant now = Instant.now();
        LOGIN_ATTEMPTS.compute(usernameOrEmail, (key, current) -> {
            if (current == null || Duration.between(current.firstAttemptAt, now).compareTo(LOGIN_WINDOW) > 0) {
                return new LoginAttemptState(1, now, null);
            }

            int updatedAttempts = current.attempts + 1;
            Instant lockedUntil = updatedAttempts >= MAX_LOGIN_ATTEMPTS ? now.plus(LOCK_DURATION) : null;
            return new LoginAttemptState(updatedAttempts, current.firstAttemptAt, lockedUntil);
        });
    }

    private void resetFailedAttempts(String usernameOrEmail) {
        LOGIN_ATTEMPTS.remove(usernameOrEmail);
    }

    private static final class LoginAttemptState {
        private final int attempts;
        private final Instant firstAttemptAt;
        private final Instant lockedUntil;

        private LoginAttemptState(int attempts, Instant firstAttemptAt, Instant lockedUntil) {
            this.attempts = attempts;
            this.firstAttemptAt = firstAttemptAt;
            this.lockedUntil = lockedUntil;
        }
    }

    private String resolveEffectiveEmail(User user) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        String username = resolveEffectiveUsername(user);
        if (username != null && SIMPLE_EMAIL_PATTERN.matcher(username).matches()) {
            return username;
        }
        return null;
    }

    private String resolveEffectiveUsername(User user) {
        String username = user.getUsername();
        if (username != null && !username.isBlank()) {
            return username;
        }
        String email = user.getEmail();
        if (email != null && !email.isBlank()) {
            int atIndex = email.indexOf('@');
            return atIndex > 0 ? email.substring(0, atIndex) : email;
        }
        return null;
    }

    private void applyLegacyIdentityFallbacks(User user, String identifier) {
        boolean changed = false;
        if ((user.getEmail() == null || user.getEmail().isBlank())
                && SIMPLE_EMAIL_PATTERN.matcher(identifier).matches()) {
            user.setEmail(identifier.toLowerCase());
            changed = true;
        }

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            if (SIMPLE_EMAIL_PATTERN.matcher(identifier).matches()) {
                int atIndex = identifier.indexOf('@');
                user.setUsername(atIndex > 0 ? identifier.substring(0, atIndex) : identifier);
            } else {
                user.setUsername(identifier);
            }
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }

    private void ensureDefaultSettings(User user) {
        generalSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    GeneralSettings defaultSettings = new GeneralSettings();
                    defaultSettings.setUser(user);
                    defaultSettings.setStockControlType(StockControlType.PER_ITEM);
                    return generalSettingsRepository.save(defaultSettings);
                });
    }

}
