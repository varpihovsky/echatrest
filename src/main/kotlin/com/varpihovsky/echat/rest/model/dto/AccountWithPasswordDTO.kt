package com.varpihovsky.echat.rest.model.dto

class AccountWithPasswordDTO(
        id: Long,
        login: String,
        val password: String,
        val authorization: AuthorizationDTO?
) : AccountWithoutPasswordDTO(id, login), DTO