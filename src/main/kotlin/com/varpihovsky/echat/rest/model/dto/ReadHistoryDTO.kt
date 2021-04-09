package com.varpihovsky.echat.rest.model.dto

import com.varpihovsky.echat.rest.model.dao.ReadHistoryDAO

class ReadHistoryDTO(
        val id: Long,
        val reader: AccountWithoutPasswordDTO,
        val message: MessageDTO,
        val status: ReadHistoryDAO.Status
) : DTO