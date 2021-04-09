package com.varpihovsky.echat.rest.model.dto

class ChatDTO(
        val id: Long,
        val name: String,
        val chatAdmins: List<AccountWithoutPasswordDTO>,
        val chatParticipants: List<AccountWithoutPasswordDTO>
) : DTO