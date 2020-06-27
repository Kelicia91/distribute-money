package com.kakaopayhw.distributemoney.service

import com.kakaopayhw.distributemoney.support.MessageKey
import com.kakaopayhw.distributemoney.domain.DistributableMoney
import com.kakaopayhw.distributemoney.domain.DistributableMoneyRepository
import com.kakaopayhw.distributemoney.domain.Distribution
import com.kakaopayhw.distributemoney.domain.DistributionRepository
import com.kakaopayhw.distributemoney.exception.DeniedAccessException
import com.kakaopayhw.distributemoney.exception.InvalidArgumentException
import com.kakaopayhw.distributemoney.exception.NotFoundEntityException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class DistributionService (
    private val distributionRepository: DistributionRepository,
    private val distributableMoneyRepository: DistributableMoneyRepository
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
        if (quotient <= 0) { throw InvalidArgumentException(MessageKey.INVALID_DISTRIBUTE) }
        val remainder = distribution.money % distribution.divisor
        return (1 until distribution.divisor).map { DistributableMoney.of(distribution, money = quotient) } + (DistributableMoney.of(distribution, money = quotient + remainder))
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun getDistributableMoney(userId: Int, roomId: String, token: String): DistributableMoney {
        val distribution = distributionRepository.findByRoomIdAndToken(roomId, token)
            ?: throw NotFoundEntityException(MessageKey.NOT_FOUND_DISTRIBUTION)

        if (userId == distribution.senderId) {
            throw DeniedAccessException(MessageKey.NOT_ALLOWED_TAKE_SELF)
        }

        if (distribution.isReceivingExpired()) {
            throw DeniedAccessException(MessageKey.TIMEOUT_TAKE_DISTRIBUTABLE_MONEY)
        }

        if (distribution.distributableMoneys.find { userId == it.receiverId } != null) {
            throw DeniedAccessException(MessageKey.ALREADY_TAKEN_DISTRIBUTABLE_MONEY)
        }

        val distributableMoney = distribution.distributableMoneys.firstOrNull { it.receiverId == null }
            ?: throw NotFoundEntityException(MessageKey.NOT_FOUND_DISTRIBUTABLE_MONEY)

        return distributableMoneyRepository.save(distributableMoney.receive(userId))
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun getDistribution(userId: Int, roomId: String, token: String): Distribution {
        val distribution = distributionRepository.findByRoomIdAndToken(roomId, token)
            ?: throw NotFoundEntityException(MessageKey.NOT_FOUND_DISTRIBUTION)

        if (userId != distribution.senderId) {
            throw DeniedAccessException(MessageKey.NOT_ALLOWED_ACCESS_OTHERS_DISTRIBUTION)
        }

        if (distribution.isCheckingExpired()) {
            throw DeniedAccessException(MessageKey.TIMEOUT_CHECK_DISTRIBUTION)
        }

        return distribution
    }
}