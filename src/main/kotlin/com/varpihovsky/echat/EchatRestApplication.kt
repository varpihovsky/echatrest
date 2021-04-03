package com.varpihovsky.echat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EchatRestApplication

fun main(args: Array<String>) {
	runApplication<EchatRestApplication>(*args)
}
