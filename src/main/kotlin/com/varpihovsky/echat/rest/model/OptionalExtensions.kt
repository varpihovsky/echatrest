package com.varpihovsky.echat.rest.model

import java.util.*

fun <T, R> Optional<T>.ifPresentMap(action: (T) -> R): R? {
    if (isPresent)
        return action.invoke(get())
    return null
}