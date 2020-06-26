package com.kakaopayhw.distributemoney.controller.interfaces

import com.kakaopayhw.distributemoney.domain.Distribution

data class DistributeMoneyRequest(
    val money: Int,
    val divisor: Int
)

data class DistributeMoneyView(
    val token: String
) {
    companion object {
        fun of(distribution: Distribution): DistributeMoneyView {
            return DistributeMoneyView(token = distribution.token)
        }
    }
}