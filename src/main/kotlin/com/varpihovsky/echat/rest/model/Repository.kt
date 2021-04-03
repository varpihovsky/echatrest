package com.varpihovsky.echat.rest.controllers.authorize

import org.springframework.data.repository.CrudRepository

interface AuthorizationRepository : CrudRepository<Authorization, Long> {
    fun findByKey(key: String): Authorization?
}

interface AccountRepository : CrudRepository<Account, Long> {
    fun findByLogin(login: String): Account?

    fun findByAuthorizationEntity(authorization: Authorization): Account?
}

interface ChatRepository : CrudRepository<Chat, Long> {
    fun findAllByName(name: String): List<Chat>?
}

interface MessageRepository : CrudRepository<Message, Long> {
    fun findAllByChat(chat: Chat): List<Message>
}

interface ReadHistoryRepository : CrudRepository<ReadEntity, Long> {
    fun findByMessage(message: Message): ReadEntity?
    fun findAllByReaderAndStatus(reader: Account, status: ReadEntity.Status): List<ReadEntity>
}