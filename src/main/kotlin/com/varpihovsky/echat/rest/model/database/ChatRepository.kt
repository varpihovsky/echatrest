package com.varpihovsky.echat.rest.model.database

import com.varpihovsky.echat.rest.model.dao.ChatDAO
import org.springframework.data.repository.CrudRepository

interface ChatRepository : CrudRepository<ChatDAO, Long> {
    fun findAllByName(name: String): List<ChatDAO>?
}