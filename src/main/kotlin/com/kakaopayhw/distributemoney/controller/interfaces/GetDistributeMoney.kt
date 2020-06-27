package com.kakaopayhw.distributemoney.controller.interfaces

import com.kakaopayhw.distributemoney.domain.DistributableMoney
import com.kakaopayhw.distributemoney.domain.Distribution
import java.time.LocalDateTime

data class TakenDistributableMoney(
    val takerId: Int,
    val takenMoney: Int
) {
    companion object {
        fun of(distributableMoney: DistributableMoney): TakenDistributableMoney {
            return TakenDistributableMoney(
                takerId = distributableMoney.receiverId ?: throw Exception(),
                takenMoney = distributableMoney.money
            )
        }
    }
}

data class DistributionView(
    val distributedAt: LocalDateTime,
    val money: Int,
    val takenMoney: Int,
    val takens: List<TakenDistributableMoney>
) {
    companion object {
        fun of(distribution: Distribution): DistributionView {
            val receivedMoneys = distribution.distributableMoneys.filter { it.receiverId != null }
            return DistributionView(
                distributedAt = distribution.createdAt ?: throw Exception(),
                money = distribution.money,
                takenMoney = receivedMoneys.map { it.money }.fold(0) { acc, money -> acc + money },
                takens = receivedMoneys.map { TakenDistributableMoney.of(it) }
            )
        }
    }
}