package com.onebite.server.split

import org.springframework.data.jpa.repository.JpaRepository

// JpaRepository를 상속하면 기본 CRUD 메서드가 자동 생성됨
// findAll(), findById(), save(), deleteById() 등
interface SplitRepository : JpaRepository<SplitRequest, Long> {
    fun findByStatus(status: SplitStatus): List<SplitRequest>
    fun findByAuthorId(userId: Long): List<SplitRequest>
}
