package com.example.demo.controllers

import com.example.demo.throttling.exceptions.TooManyRequestsException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ControllerExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(TooManyRequestsException::class)
    @ResponseBody
    fun tooManyRequestsHandler(ex: TooManyRequestsException): ResponseEntity<String> {
        return ResponseEntity(ex.message, ex.httpStatus)
    }
}