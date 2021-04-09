package com.varpihovsky.echat.rest.model.database

import com.varpihovsky.echat.rest.model.dao.AccountDAO
import com.varpihovsky.echat.rest.model.dao.AuthorizationDAO
import org.springframework.data.repository.CrudRepository

interface AccountRepository : CrudRepository<AccountDAO, Long> {
    fun findByLogin(login: String): AccountDAO?

    fun findByAuthorizationDAOEntity(authorizationDAO: AuthorizationDAO): AccountDAO?
}