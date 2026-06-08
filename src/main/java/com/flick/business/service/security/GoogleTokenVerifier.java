package com.flick.business.service.security;

import com.flick.business.api.dto.auth.GoogleTokenPayload;
import com.flick.business.exception.InvalidTokenException;
import com.flick.business.res.GoogleAuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

/**
 * Serviço para validar ID tokens do Google
 * Faz requisição à API do Google para verificar assinatura e extrair claims
 */
@Service
public class GoogleTokenVerifier {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenVerifier.class);
    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    
    private final GoogleAuthProperties googleAuthProperties;
    private final ObjectMapper objectMapper;

    public GoogleTokenVerifier(GoogleAuthProperties googleAuthProperties, ObjectMapper objectMapper) {
        this.googleAuthProperties = googleAuthProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Valida um ID token do Google contra a API do Google
     * @param idToken O ID token a validar
     * @return GoogleTokenPayload com os dados extraídos do token
     * @throws InvalidTokenException Se o token for inválido, expirado ou não passar na validação
     */
    public GoogleTokenPayload verifyToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new InvalidTokenException("ID token é obrigatório");
        }

        try {
            // 1. Fazer requisição à API do Google para validar o token
            GoogleTokenPayload tokenPayload = fetchTokenInfo(idToken);

            // 2. Validar se o token não está expirado
            if (isTokenExpired(tokenPayload.getExp())) {
                throw new InvalidTokenException("ID token expirado");
            }

            // 3. Validar se o audience (aud) corresponde ao nosso client ID
            if (!tokenPayload.getAud().equals(googleAuthProperties.getClientId())) {
                logger.warn("Token audience mismatch. Expected: {}, Got: {}", 
                    googleAuthProperties.getClientId(), tokenPayload.getAud());
                throw new InvalidTokenException("ID token não é válido para esta aplicação");
            }

            // 4. Validar se o email foi verificado
            if (!tokenPayload.isEmailVerified()) {
                logger.warn("Token has unverified email: {}", tokenPayload.getEmail());
                throw new InvalidTokenException("Email não foi verificado no Google");
            }

            logger.info("Token validation successful for user: {}", tokenPayload.getEmail());
            return tokenPayload;

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao validar token Google", e);
            throw new InvalidTokenException("Erro ao processar autenticação com Google", e);
        }
    }

    /**
     * Faz requisição HTTP à API tokeninfo do Google
     */
    private GoogleTokenPayload fetchTokenInfo(String idToken) throws InvalidTokenException {
        try {
            URL url = new URL(TOKENINFO_URL + idToken);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                // Token válido
                String response = new String(connection.getInputStream().readAllBytes());
                return objectMapper.readValue(response, GoogleTokenPayload.class);
            } else if (responseCode == 400) {
                // Token inválido ou expirado
                throw new InvalidTokenException("ID token inválido ou expirado");
            } else {
                throw new InvalidTokenException("Erro ao validar token com Google (HTTP " + responseCode + ")");
            }

        } catch (InvalidTokenException e) {
            throw e;
        } catch (IOException e) {
            logger.error("Erro na requisição HTTP para tokeninfo", e);
            throw new InvalidTokenException("Erro ao conectar com Google para validar token", e);
        }
    }

    /**
     * Verifica se o token expirou
     */
    private boolean isTokenExpired(long expirationTime) {
        long currentTime = Instant.now().getEpochSecond();
        return currentTime > expirationTime;
    }
}
