package com.soiltech.backend.infrastructure.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class WikipediaImageService {

    private val log = LoggerFactory.getLogger(javaClass)
    private val rest = RestTemplate()
    private val headers = HttpHeaders().apply {
        set(HttpHeaders.USER_AGENT, "SoilTechLBC/1.0 (soiltech-backend; contact@soiltechlbc.com)")
    }

    fun fetchThumbnailUrl(cropName: String, widthPx: Int = 400): String? {
        return try {
            val encoded = URLEncoder.encode(cropName, StandardCharsets.UTF_8)
            val url = "https://en.wikipedia.org/w/api.php" +
                "?action=query&titles=$encoded&prop=pageimages" +
                "&pithumbsize=$widthPx&format=json&redirects=1"

            val response = rest.exchange(url, HttpMethod.GET, HttpEntity<Unit>(headers), Map::class.java)
                .body ?: return null

            @Suppress("UNCHECKED_CAST")
            val pages = (response["query"] as? Map<String, Any>)
                ?.get("pages") as? Map<String, Any>
                ?: return null

            pages.values.firstOrNull()
                ?.let { it as? Map<String, Any> }
                ?.get("thumbnail")
                ?.let { it as? Map<String, Any> }
                ?.get("source").let { (it as? String)?.takeIf { s -> s.isNotBlank() } }
        } catch (e: Exception) {
            log.warn("Wikipedia image fetch failed for '{}': {}", cropName, e.message)
            null
        }
    }
}
