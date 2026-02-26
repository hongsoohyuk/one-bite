package com.onebite.server.split

import org.springframework.stereotype.Service
import kotlin.math.*

@Service
class SplitService(
    private val splitRepository: SplitRepository
) {
    fun create(dto: CreateSplitDto): SplitResponse {
        val entity = SplitRequest(
            productName = dto.productName,
            totalPrice = dto.totalPrice,
            totalQty = dto.totalQty,
            splitCount = dto.splitCount,
            imageUrl = dto.imageUrl,
            latitude = dto.latitude,
            longitude = dto.longitude,
            address = dto.address
        )
        return SplitResponse.from(splitRepository.save(entity))
    }

    fun findAll(): List<SplitResponse> =
        splitRepository.findAll().map { SplitResponse.from(it) }

    fun findById(id: Long): SplitResponse =
        splitRepository.findById(id)
            .map { SplitResponse.from(it) }
            .orElseThrow { NoSuchElementException("Split request not found: $id") }

    fun findByStatus(status: SplitStatus): List<SplitResponse> =
        splitRepository.findByStatus(status).map { SplitResponse.from(it) }

    fun findNearby(lat: Double, lng: Double, radiusKm: Double = 3.0): List<SplitResponse> =
        splitRepository.findNearby(lat, lng, radiusKm).map { entity ->
            val distance = haversineDistance(lat, lng, entity.latitude, entity.longitude)
            SplitResponse.from(entity, distance)
        }

    fun cancel(id: Long): SplitResponse {
        val entity = splitRepository.findById(id)
            .orElseThrow { NoSuchElementException("Split request not found: $id") }
        entity.status = SplitStatus.CANCELLED
        return SplitResponse.from(splitRepository.save(entity))
    }

    private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return r * c
    }
}
