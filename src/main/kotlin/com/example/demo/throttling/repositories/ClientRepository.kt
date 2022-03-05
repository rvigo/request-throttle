package com.example.demo.throttling.repositories

import com.example.demo.throttling.entities.Client
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository : CrudRepository<Client, String>
