package com.soiltech.backend.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(private val jwtProperties: JwtProperties) {

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray())
    }

    fun generateAccessToken(userId: UUID, email: String, role: String): String =
        buildToken(userId, email, role, jwtProperties.accessTokenExpiration)

    fun generateRefreshToken(userId: UUID, email: String, role: String): String =
        buildToken(userId, email, role, jwtProperties.refreshTokenExpiration)

    private fun buildToken(userId: UUID, email: String, role: String, expiry: Long): String =
        Jwts.builder()
            .subject(email)
            .claim("userId", userId.toString())
            .claim("role", role)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiry))
            .signWith(signingKey)
            .compact()

    fun extractEmail(token: String): String = extractClaims(token).subject

    fun extractUserId(token: String): UUID = UUID.fromString(
        extractClaims(token)["userId"] as String
    )

    fun extractRole(token: String): String = extractClaims(token)["role"] as String

    fun isTokenValid(token: String): Boolean = try {
        extractClaims(token).expiration.after(Date())
    } catch (e: JwtException) {
        false
    } catch (e: IllegalArgumentException) {
        false
    }

    private fun extractClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
