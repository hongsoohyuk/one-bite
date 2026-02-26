package com.onebite.server.split

import com.onebite.server.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class SplitService(
    private val splitRepository: SplitRepository,
    private val splitParticipantRepository: SplitParticipantRepository,
    private val userRepository: UserRepository
) {
    fun create(dto: CreateSplitDto, userId: Long): SplitResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다: $userId") }

        val entity = SplitRequest(
            user = user,
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
        splitRepository.findAll().map { toResponse(it) }

    fun findById(id: Long): SplitResponse {
        val entity = splitRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Split을 찾을 수 없습니다: $id") }
        return toResponse(entity)
    }

    fun findByStatus(status: SplitStatus): List<SplitResponse> =
        splitRepository.findByStatus(status).map { toResponse(it) }

    fun join(splitId: Long, userId: Long): SplitResponse {
        val split = splitRepository.findById(splitId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Split을 찾을 수 없습니다: $splitId") }

        if (split.status != SplitStatus.WAITING) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "WAITING 상태의 Split만 참여할 수 있습니다")
        }

        if (split.user.id == userId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "본인이 등록한 Split에는 참여할 수 없습니다")
        }

        if (splitParticipantRepository.existsBySplitRequestIdAndUserId(splitId, userId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 참여한 Split입니다")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다: $userId") }

        splitParticipantRepository.save(SplitParticipant(splitRequest = split, user = user))

        val participantCount = splitParticipantRepository.countBySplitRequestId(splitId)
        if (participantCount >= split.splitCount - 1) {
            split.status = SplitStatus.MATCHED
            splitRepository.save(split)
        }

        val participants = splitParticipantRepository.findBySplitRequestId(splitId)
        return SplitResponse.from(split, participants)
    }

    fun cancel(id: Long, userId: Long): SplitResponse {
        val entity = splitRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Split을 찾을 수 없습니다: $id") }

        if (entity.user.id != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 취소할 수 있습니다")
        }

        if (entity.status != SplitStatus.WAITING) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "WAITING 상태의 Split만 취소할 수 있습니다")
        }

        entity.status = SplitStatus.CANCELLED
        val saved = splitRepository.save(entity)
        val participants = splitParticipantRepository.findBySplitRequestId(id)
        return SplitResponse.from(saved, participants)
    }

    private fun toResponse(entity: SplitRequest): SplitResponse {
        val participants = splitParticipantRepository.findBySplitRequestId(entity.id)
        return SplitResponse.from(entity, participants)
    }
}
