package com.onebite.server.split

import com.onebite.server.user.User
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

// ── Request DTO: 클라이언트가 보내는 데이터 ──
// Next.js에서 req.body를 타입으로 정의하는 것과 같음
data class CreateSplitDto(
    @field:NotBlank
    val productName: String,

    @field:Min(1)
    val totalPrice: Int,

    @field:Min(1)
    val totalQty: Int,

    @field:Min(2)
    val splitCount: Int,

    val imageUrl: String? = null,
    val latitude: Double,
    val longitude: Double,
    val address: String
)

// ── Author DTO: 작성자 정보 ──
data class AuthorDto(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?
) {
    companion object {
        fun from(user: User) = AuthorDto(
            id = user.id,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl
        )
    }
}

// ── Response DTO: 클라이언트에 보내는 데이터 ──
data class SplitResponse(
    val id: Long,
    val productName: String,
    val totalPrice: Int,
    val totalQty: Int,
    val splitCount: Int,
    val pricePerPerson: Int,
    val qtyPerPerson: Int,
    val imageUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val status: SplitStatus,
    val author: AuthorDto,
    val createdAt: String
) {
    companion object {
        fun from(entity: SplitRequest) = SplitResponse(
            id = entity.id,
            productName = entity.productName,
            totalPrice = entity.totalPrice,
            totalQty = entity.totalQty,
            splitCount = entity.splitCount,
            pricePerPerson = entity.totalPrice / entity.splitCount,
            qtyPerPerson = entity.totalQty / entity.splitCount,
            imageUrl = entity.imageUrl,
            latitude = entity.latitude,
            longitude = entity.longitude,
            address = entity.address,
            status = entity.status,
            author = AuthorDto.from(entity.author),
            createdAt = entity.createdAt.toString()
        )
    }
}
