package com.kakaopayhw.distributemoney.controller.interfaces

import com.kakaopayhw.distributemoney.domain.DistributableMoney

data class DistributableMoneyView(
    val money: Int
) {
    companion object {
        fun of(distributableMoney: DistributableMoney): DistributableMoneyView {
            return DistributableMoneyView(money = distributableMoney.money)
        }
    }
}