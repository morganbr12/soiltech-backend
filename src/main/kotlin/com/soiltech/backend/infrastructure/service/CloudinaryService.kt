package com.soiltech.backend.infrastructure.service

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryService(
    @Value("\${cloudinary.cloud-name}") cloudName: String,
    @Value("\${cloudinary.api-key}") apiKey: String,
    @Value("\${cloudinary.api-secret}") apiSecret: String
) {
    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to cloudName,
            "api_key" to apiKey,
            "api_secret" to apiSecret,
            "secure" to true
        )
    )

    fun uploadImage(file: MultipartFile, folder: String): String {
        val result = cloudinary.uploader().upload(
            file.bytes,
            mapOf(
                "folder" to folder,
                "resource_type" to "image"
            )
        )
        return result["secure_url"] as String
    }
}
