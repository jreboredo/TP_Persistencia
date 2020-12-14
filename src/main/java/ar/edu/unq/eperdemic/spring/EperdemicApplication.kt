package ar.edu.unq.eperdemic.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EperdemicApplication

fun main(args: Array<String>) {
	runApplication<EperdemicApplication>(*args)
}
