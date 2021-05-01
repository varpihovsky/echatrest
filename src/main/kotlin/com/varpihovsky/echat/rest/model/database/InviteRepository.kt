package com.varpihovsky.echat.rest.model.database

import com.varpihovsky.echat.rest.model.dao.AccountDAO
import com.varpihovsky.echat.rest.model.dao.InviteDAO
import org.springframework.data.repository.CrudRepository

interface InviteRepository : CrudRepository<InviteDAO, Long> {
    fun getAllByAccount(accountDAO: AccountDAO): List<InviteDAO>
    fun getById(id: Long): InviteDAO?
}