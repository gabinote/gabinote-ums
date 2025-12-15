package com.gabinote.ums.common.util.controller

import org.springframework.http.ResponseEntity

object ResponseEntityHelper {

    fun noContent(): ResponseEntity<Void> {
        return ResponseEntity.noContent().build()
    }

    fun <T> created(body: T): ResponseEntity<T> {
        return ResponseEntity.status(201).body(body)
    }

}