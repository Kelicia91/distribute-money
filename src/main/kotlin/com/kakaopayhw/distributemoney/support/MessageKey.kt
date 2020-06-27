package com.kakaopayhw.distributemoney.support

enum class MessageKey constructor(private val messageSourceKey: String) {
    EXCEPTION("exception"),

    INVALID_DISTRIBUTE("invalid.distribute"),
    INVALID_DISTRIBUTE_MONEY("invalid.distribute.money"),
    INVALID_DISTRIBUTE_DIVISOR("invalid.distribute.divisor"),

    NOT_FOUND_DISTRIBUTION("not.found.distribution"),
    NOT_FOUND_DISTRIBUTABLE_MONEY("not.found.distributable.money"),

    NOT_ALLOWED_TAKE_SELF("not.allowed.take.self"),
    ALREADY_TAKEN_DISTRIBUTABLE_MONEY("already.taken.distributable.money"),
    TIMEOUT_TAKE_DISTRIBUTABLE_MONEY("time.out.take.distributable.money"),

    NOT_ALLOWED_ACCESS_OTHERS_DISTRIBUTION("not.allowed.access.others.distribution"),
    TIMEOUT_CHECK_DISTRIBUTION("time.out.check.distribution");

    fun getMessageSourceKey(): String {
        return messageSourceKey
    }
}