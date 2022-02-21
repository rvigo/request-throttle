package com.example.demo.services

import com.example.demo.entities.DemoEntity
import org.springframework.stereotype.Service
import java.util.*
import java.util.UUID.randomUUID

@Service
class DemoService {
    fun execute(): DemoEntity {
        val random = Random()
        return DemoEntity(
            id = randomUUID(),
            name = "test",
            age = random.nextInt(50)
        )
    }
}