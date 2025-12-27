package com.gabinote.ums.outbox.dto.service

data class OutBoxCreateReqServiceDto(
    val eventType: String,
    val payload: String,
)