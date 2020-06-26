package com.kakaopayhw.distributemoney.controller.interfaces

enum class MessageKey constructor(private val messageSourceKey: String) {
    EXCEPTION("exception"),

    INVALID_DISTRIBUTE_MONEY("invalid.distribute.money"),
    INVALID_DISTRIBUTE_DIVISOR("invalid.distribute.divisor");

    fun getMessageSourceKey(): String {
        return messageSourceKey
    }
}