package com.example.demo.throttling.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Client(@Id val name: String, val rate: Int)
