package com.varpihovsky.echat.rest.model.database

import com.varpihovsky.echat.rest.model.dao.ChatDAO
import com.varpihovsky.echat.rest.model.dao.MessageDAO
import org.springframework.data.repository.CrudRepository

interface MessageRepository : CrudRepository<MessageDAO, Long> {
    fun findAllByChatDAO(chatDAO: ChatDAO): List<MessageDAO>
}