package com.kakaopayhw.distributemoney.exception

import com.kakaopayhw.distributemoney.support.MessageKey

open class DistributionException(
        var messageKey: MessageKey,
        vararg objs: Any
): RuntimeException (
    messageKey.name.plus(":").plus(objs.joinToString { it.toString() })
) {
    var objs: Array<*> = objs
}

class InvalidArgumentException: DistributionException {
    constructor(messageKey: MessageKey, vararg objs: Any) : super(messageKey, *objs)
}

class NotFoundEntityException: DistributionException {
    constructor(messageKey: MessageKey, vararg objs: Any) : super(messageKey, *objs)
}

class DeniedAccessException: DistributionException {
    constructor(messageKey: MessageKey, vararg objs: Any) : super(messageKey, *objs)
}