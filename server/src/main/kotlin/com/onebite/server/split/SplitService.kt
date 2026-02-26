package com.onebite.server.split

import com.onebite.server.user.UserRepository
import org.springframework.stereotype.Service

@Service
class SplitService(
    private val splitRepository: SplitRepository,
    private val userRepository: UserRepository
) {
    fun create(dto: CreateSplitDto, userId: Long): SplitResponse {
        val author = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found: $userId") }

        val entity = SplitRequest(
            productName = dto.productName,
            totalPrice = dto.totalPrice,
            totalQty = dto.totalQty,
            splitCount = dto.splitCount,
            imageUrl = dto.imageUrl,
            latitude = dto.latitude,
            longitude = dto.longitude,
            address = dto.address,
            author = author
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

    fun findByAuthorId(userId: Long): List<SplitResponse> =
        splitRepository.findByAuthorId(userId).map { SplitResponse.from(it) }

    fun cancel(id: Long): SplitResponse {
        val entity = splitRepository.findById(id)
            .orElseThrow { NoSuchElementException("Split request not found: $id") }
        entity.status = SplitStatus.CANCELLED
        return SplitResponse.from(splitRepository.save(entity))
    }
}
