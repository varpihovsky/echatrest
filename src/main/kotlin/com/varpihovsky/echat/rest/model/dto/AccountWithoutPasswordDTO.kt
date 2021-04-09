package com.varpihovsky.echat.rest.model.dto

open class AccountWithoutPasswordDTO(
        val id: Long,
        val login: String
) : DTO