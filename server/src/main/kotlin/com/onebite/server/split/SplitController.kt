package com.onebite.server.split

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

// Next.js 대응: app/api/splits/route.ts
@RestController
@RequestMapping("/api/splits")
class SplitController(
    private val splitService: SplitService
) {
    // GET /api/splits?lat=37.5&lng=126.9&radiusKm=3&status=WAITING
    @GetMapping
    fun getAll(
        @RequestParam status: SplitStatus? = null,
        @RequestParam lat: Double? = null,
        @RequestParam lng: Double? = null,
        @RequestParam radiusKm: Double? = null
    ): List<SplitResponse> =
        if (lat != null && lng != null)
            splitService.findNearby(lat, lng, radiusKm ?: 3.0)
        else if (status != null)
            splitService.findByStatus(status)
        else
            splitService.findAll()

    // GET /api/splits/{id}
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): SplitResponse =
        splitService.findById(id)

    // POST /api/splits
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: CreateSplitDto, authentication: Authentication): SplitResponse {
        val userId = authentication.principal as Long
        return splitService.create(dto, userId)
    }

    // GET /api/splits/my
    @GetMapping("/my")
    fun getMy(authentication: Authentication): List<SplitResponse> {
        val userId = authentication.principal as Long
        return splitService.findByAuthorId(userId)
    }

    // POST /api/splits/{id}/join
    @PostMapping("/{id}/join")
    fun join(@PathVariable id: Long, authentication: Authentication): SplitResponse {
        val userId = authentication.principal as Long
        return splitService.join(id, userId)
    }

    // PATCH /api/splits/{id}/cancel
    @PatchMapping("/{id}/cancel")
    fun cancel(@PathVariable id: Long, authentication: Authentication): SplitResponse {
        val userId = authentication.principal as Long
        return splitService.cancel(id, userId)
    }
}
