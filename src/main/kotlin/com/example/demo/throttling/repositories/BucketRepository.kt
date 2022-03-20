package com.example.demo.throttling.repositories

import com.example.demo.throttling.entities.Bucket
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BucketRepository : CrudRepository<Bucket, String>
