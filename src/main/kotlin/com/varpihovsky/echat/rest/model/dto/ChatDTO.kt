package com.varpihovsky.echat.rest.model.dto

import com.varpihovsky.echat.rest.model.dao.ChatDAO

class ChatDTO(
    val id: Long,
    val name: String,
    val chatAdmins: List<AccountWithoutPasswordDTO>,
    val chatParticipants: List<AccountWithoutPasswordDTO>,
    val type: ChatDAO.Type
) : DTO