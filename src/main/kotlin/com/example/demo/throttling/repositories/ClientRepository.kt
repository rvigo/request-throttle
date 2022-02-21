package com.example.demo.throttling.repositories

import com.example.demo.throttling.entity.Client
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository : MongoRepository<Client, String>
