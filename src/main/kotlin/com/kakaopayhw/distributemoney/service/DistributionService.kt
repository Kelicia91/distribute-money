package com.kakaopayhw.distributemoney.service

import com.kakaopayhw.distributemoney.controller.interfaces.MessageKey
import com.kakaopayhw.distributemoney.domain.DistributableMoney
import com.kakaopayhw.distributemoney.domain.Distribution
import com.kakaopayhw.distributemoney.domain.DistributionRepository
import com.kakaopayhw.distributemoney.exception.InvalidArgumentException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DistributionService (
    private val distributionRepository: DistributionRepository
) {
    @Transactional
    fun addDistribution(src: Distribution): Distribution {
        if (src.money <= 0) { throw InvalidArgumentException(MessageKey.INVALID_DISTRIBUTE_MONEY) }
        if (src.divisor <= 0) { throw InvalidArgumentException(MessageKey.INVALID_DISTRIBUTE_DIVISOR) }

        return distributionRepository.save(
            src.apply { this.distributableMoneys = generate(this) }
        )
    }

    private fun generate(distribution: Distribution): List<DistributableMoney> {
        val quotient = distribution.money / distribution.divisor
        val remainder = distribution.money % distribution.divisor
        return (1 until distribution.divisor).map { DistributableMoney.of(distribution, money = quotient) } + (DistributableMoney.of(distribution, money = quotient + remainder))
    }
}