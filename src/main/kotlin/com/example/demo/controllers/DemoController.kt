package com.example.demo.controllers

import com.example.demo.entities.DemoEntity
import com.example.demo.services.DemoService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DemoController(val service: DemoService) {
    @GetMapping("/", produces = [APPLICATION_JSON_VALUE])
    fun get(): DemoEntity {
        return service.execute()
    }
}