package com.varpihovsky.echat.rest.model.database

import com.varpihovsky.echat.rest.model.dao.AccountDAO
import com.varpihovsky.echat.rest.model.dao.MessageDAO
import com.varpihovsky.echat.rest.model.dao.ReadHistoryDAO
import org.springframework.data.repository.CrudRepository

interface ReadHistoryRepository : CrudRepository<ReadHistoryDAO, Long> {
    fun findByMessageDAO(messageDAO: MessageDAO): ReadHistoryDAO?
    fun findByMessageDAOAndReader(messageDAO: MessageDAO, reader: AccountDAO): ReadHistoryDAO?
    fun findAllByReaderAndStatus(reader: AccountDAO, status: ReadHistoryDAO.Status): List<ReadHistoryDAO>
}