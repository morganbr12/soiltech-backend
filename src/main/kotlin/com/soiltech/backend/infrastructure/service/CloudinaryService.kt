package com.soiltech.backend.infrastructure.service

import com.cloudinary.Cloudinary
import com.cloudinary.util.ObjectUtils
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
        ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        )
    )

    fun uploadImage(file: MultipartFile, folder: String): String {
        val result = cloudinary.uploader().upload(
            file.bytes,
            ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image"
            )
        )
        return result["secure_url"] as String
    }
}
