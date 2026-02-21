package com.onebite.server.split

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

// Next.js 대응: app/api/splits/route.ts
@RestController
@RequestMapping("/api/splits")
class SplitController(
    private val splitService: SplitService
) {
    // GET /api/splits
    @GetMapping
    fun getAll(@RequestParam status: SplitStatus? = null): List<SplitResponse> =
        if (status != null) splitService.findByStatus(status)
        else splitService.findAll()

    // GET /api/splits/{id}
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): SplitResponse =
        splitService.findById(id)

    // POST /api/splits
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: CreateSplitDto): SplitResponse =
        splitService.create(dto)

    // PATCH /api/splits/{id}/cancel
    @PatchMapping("/{id}/cancel")
    fun cancel(@PathVariable id: Long): SplitResponse =
        splitService.cancel(id)
}
