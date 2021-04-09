package com.varpihovsky.echat.rest.model.dto

import java.util.*

class AuthorizationDTO(
        val id: Long,
        val key: String,
        val created: Date
) : DTO