package com.varpihovsky.echat.rest.model.database

import com.varpihovsky.echat.rest.model.dao.AuthorizationDAO
import org.springframework.data.repository.CrudRepository

interface AuthorizationRepository : CrudRepository<AuthorizationDAO, Long> {
    fun findByKey(key: String): AuthorizationDAO?
}