package com.varpihovsky.echat.rest.model.dto

class InviteDTO(
    val id: Long,
    val account: AccountWithoutPasswordDTO,
    val chat: ChatDTO
) : DTO