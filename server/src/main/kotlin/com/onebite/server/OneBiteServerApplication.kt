package com.onebite.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OneBiteServerApplication

fun main(args: Array<String>) {
	runApplication<OneBiteServerApplication>(*args)
}
