package com.onebite.server.split

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

// JpaRepository를 상속하면 기본 CRUD 메서드가 자동 생성됨
// findAll(), findById(), save(), deleteById() 등
interface SplitRepository : JpaRepository<SplitRequest, Long> {
    fun findByStatus(status: SplitStatus): List<SplitRequest>

    @Query(
        value = """
            SELECT * FROM split_requests s
            WHERE (6371 * ACOS(
                COS(RADIANS(:lat)) * COS(RADIANS(s.latitude))
                * COS(RADIANS(s.longitude) - RADIANS(:lng))
                + SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))
            )) <= :radiusKm
            AND s.status = 'WAITING'
            ORDER BY (6371 * ACOS(
                COS(RADIANS(:lat)) * COS(RADIANS(s.latitude))
                * COS(RADIANS(s.longitude) - RADIANS(:lng))
                + SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))
            )) ASC
        """,
        nativeQuery = true
    )
    fun findNearby(
        @Param("lat") lat: Double,
        @Param("lng") lng: Double,
        @Param("radiusKm") radiusKm: Double
    ): List<SplitRequest>
}
