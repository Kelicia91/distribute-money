package com.kakaopayhw.distributemoney.support

import java.security.SecureRandom

object RandomUtil {
    private val random = SecureRandom()

    fun divideForPositive(target: Int, divisor: Int): List<Int> {
        val randomNumbers = mutableListOf<Int>()

        var remain = target - divisor
        for (i in 1 until divisor) {
            val randomNumber = if (remain > 0) random.nextInt(remain) else 0
            randomNumbers.add(randomNumber + 1)
            remain -= randomNumber
        }

        randomNumbers.add(remain + 1)

        return randomNumbers
    }
}