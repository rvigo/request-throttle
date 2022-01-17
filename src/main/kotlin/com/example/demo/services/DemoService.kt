package com.example.demo.services

import com.example.demo.entities.DemoEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class DemoService {
    fun execute(): DemoEntity {
        return DemoEntity(UUID.randomUUID(), "test", 29)
    }
}