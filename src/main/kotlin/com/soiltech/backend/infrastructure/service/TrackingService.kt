package com.soiltech.backend.infrastructure.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class TrackingService(private val objectMapper: ObjectMapper) {

    private val emitters = ConcurrentHashMap<String, SseEmitter>()

    fun subscribe(): SseEmitter {
        val id = UUID.randomUUID().toString()
        val emitter = SseEmitter(Long.MAX_VALUE)
        emitters[id] = emitter
        emitter.onCompletion { emitters.remove(id) }
        emitter.onTimeout { emitters.remove(id) }
        emitter.onError { emitters.remove(id) }
        return emitter
    }

    fun broadcast(vehicleId: UUID, lat: Double, lng: Double, speed: Double, heading: Double, timestamp: LocalDateTime) {
        val payload = mapOf(
            "vehicleId" to vehicleId,
            "lat" to lat,
            "lng" to lng,
            "speed" to speed,
            "heading" to heading,
            "timestamp" to timestamp.toString()
        )
        val json = objectMapper.writeValueAsString(payload)
        val dead = mutableListOf<String>()
        emitters.forEach { (id, emitter) ->
            try {
                emitter.send(SseEmitter.event().name("position").data(json))
            } catch (e: Exception) {
                dead.add(id)
            }
        }
        dead.forEach { emitters.remove(it) }
    }
}
