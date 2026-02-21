package com.onebite.server.split

import org.springframework.stereotype.Service

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

    fun cancel(id: Long): SplitResponse {
        val entity = splitRepository.findById(id)
            .orElseThrow { NoSuchElementException("Split request not found: $id") }
        entity.status = SplitStatus.CANCELLED
        return SplitResponse.from(splitRepository.save(entity))
    }
}
