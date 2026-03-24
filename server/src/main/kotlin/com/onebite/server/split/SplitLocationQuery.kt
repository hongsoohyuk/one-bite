package com.onebite.server.split

import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

interface SplitLocationQuery {
    fun findNearby(lat: Double, lng: Double, radiusKm: Double, pageable: Pageable): Page<SplitRequest>
}

@Component
@Profile("!prod")
class H2SplitLocationQuery(
    private val splitRepository: SplitRepository
) : SplitLocationQuery {
    override fun findNearby(lat: Double, lng: Double, radiusKm: Double, pageable: Pageable): Page<SplitRequest> =
        splitRepository.findNearby(lat, lng, radiusKm, pageable)
}

@Component
@Profile("prod")
class PostgisSplitLocationQuery(
    private val entityManager: EntityManager
) : SplitLocationQuery {
    override fun findNearby(lat: Double, lng: Double, radiusKm: Double, pageable: Pageable): Page<SplitRequest> {
        val radiusMeters = radiusKm * 1000

        val countSql = """
            SELECT COUNT(*) FROM split_requests s
            WHERE s.status = 'WAITING'
            AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radius)
        """
        val total = (entityManager.createNativeQuery(countSql)
            .setParameter("lat", lat)
            .setParameter("lng", lng)
            .setParameter("radius", radiusMeters)
            .singleResult as Number).toLong()

        val dataSql = """
            SELECT s.* FROM split_requests s
            WHERE s.status = 'WAITING'
            AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radius)
            ORDER BY ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) ASC
        """
        @Suppress("UNCHECKED_CAST")
        val results = entityManager.createNativeQuery(dataSql, SplitRequest::class.java)
            .setParameter("lat", lat)
            .setParameter("lng", lng)
            .setParameter("radius", radiusMeters)
            .setFirstResult(pageable.offset.toInt())
            .setMaxResults(pageable.pageSize)
            .resultList as List<SplitRequest>

        return PageImpl(results, pageable, total)
    }
}
