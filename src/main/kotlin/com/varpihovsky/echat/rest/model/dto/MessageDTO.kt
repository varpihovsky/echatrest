package com.varpihovsky.echat.rest.model.dto

import java.util.*

class MessageDTO(
        val id: Long,
        val text: String,
        val sender: AccountWithoutPasswordDTO?,
        val chat: ChatDTO,
        val receiver: MessageDTO?,
        val created: Date
) : DTO