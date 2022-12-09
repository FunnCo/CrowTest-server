package com.funnco.crowtestserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CrowTestServerApplication

fun main(args: Array<String>) {
	runApplication<CrowTestServerApplication>(*args)
}
