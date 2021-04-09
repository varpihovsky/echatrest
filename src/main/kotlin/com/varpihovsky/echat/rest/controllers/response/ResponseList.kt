package com.varpihovsky.echat.rest.controllers.response

import com.varpihovsky.echat.rest.model.dto.DTO

data class ResponseList<T : DTO>(val response: List<T>)