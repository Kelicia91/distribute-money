package com.kakaopayhw.distributemoney.service

import com.kakaopayhw.distributemoney.controller.interfaces.DistributeMoneyRequest
import com.kakaopayhw.distributemoney.domain.DistributableMoneyRepository
import com.kakaopayhw.distributemoney.domain.Distribution
import com.kakaopayhw.distributemoney.domain.DistributionRepository
import com.kakaopayhw.distributemoney.exception.InvalidArgumentException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DistributionServiceTest {
    @Autowired
    lateinit var distributionRepository: DistributionRepository

    @Autowired
    lateinit var distributableMoneyRepository: DistributableMoneyRepository

    @Autowired
    lateinit var service: DistributionService

    @BeforeEach
    fun beforeEach() {
        distributableMoneyRepository.deleteAll()
        distributionRepository.deleteAll()
    }

    @Test
    fun `addDistribution - generate`() {
        val money = 1000
        val divisor = 3
        val src = Distribution.of(userId = 1, roomId = "room", request = DistributeMoneyRequest(money, divisor))
        val added = service.addDistribution(src)

        assertTrue { added.distributableMoneys.all { it.id != null && it.distribution.id == added.id } }
        assertEquals(divisor, added.distributableMoneys.size)
        assertTrue { money == added.distributableMoneys.map { it.money }.reduce { acc, money -> acc + money } }
    }

    @Test
    fun `addDistribution - invalid money`() {
        val invalidMoney = -1
        assertThrows<InvalidArgumentException> {
            service.addDistribution(
                Distribution.of(userId = 1, roomId = "room", request = DistributeMoneyRequest(invalidMoney, 3))
            )
        }
    }

    @Test
    fun `addDistribution - invalid divisor`() {
        val invalidDivisor = 0
        assertThrows<InvalidArgumentException> {
            service.addDistribution(
                Distribution.of(userId = 1, roomId = "room", request = DistributeMoneyRequest(100, invalidDivisor))
            )
        }
    }
}