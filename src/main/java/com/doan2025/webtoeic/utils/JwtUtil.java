package com.doan2025.webtoeic.utils;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.exception.WebToeicException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new WebToeicException(ResponseCode.CANNOT_GET, ResponseObject.TOKEN);
    }

    public String getEmailFromToken(HttpServletRequest request) {
        try {
            String token = getJwtFromRequest(request);
            SecretKey key = Keys.hmacShaKeyFor(SIGNER_KEY.getBytes());
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return jws.getBody().getSubject();
        } catch (JwtException e) {
            // Xử lý lỗi nếu token không hợp lệ
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public String getEmailFromTokenString(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(SIGNER_KEY.getBytes());
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return jws.getBody().getSubject();
        } catch (JwtException e) {
            // Xử lý lỗi nếu token không hợp lệ
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public SecretKey getSigningKey() {
        if (SIGNER_KEY == null) {
            throw new IllegalStateException("signerKey is null. Please check the configuration for jwt.signerKey");
        }
        byte[] keyBytes = Decoders.BASE64.decode(SIGNER_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
