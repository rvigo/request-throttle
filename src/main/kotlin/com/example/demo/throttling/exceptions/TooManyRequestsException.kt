package com.example.demo.throttling.exceptions

import org.springframework.http.HttpStatus

class TooManyRequestsException(override val message: String, val httpStatus: HttpStatus) : FilterBaseException()