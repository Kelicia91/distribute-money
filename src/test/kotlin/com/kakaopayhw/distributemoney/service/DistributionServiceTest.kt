package com.kakaopayhw.distributemoney.service

import com.kakaopayhw.distributemoney.controller.interfaces.DistributeMoneyRequest
import com.kakaopayhw.distributemoney.domain.DistributableMoneyRepository
import com.kakaopayhw.distributemoney.domain.Distribution
import com.kakaopayhw.distributemoney.domain.DistributionRepository
import com.kakaopayhw.distributemoney.exception.DeniedAccessException
import com.kakaopayhw.distributemoney.exception.InvalidArgumentException
import com.kakaopayhw.distributemoney.exception.NotFoundEntityException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

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
        assertTrue { money == added.distributableMoneys.map { it.money }.fold(0) { acc, money -> acc + money } }
    }

    @Test
    fun `addDistribution - INVALID_DISTRIBUTE_MONEY`() {
        val invalidMoney = -1
        assertThrows<InvalidArgumentException> {
            service.addDistribution(
                Distribution.of(userId = 1, roomId = "room", request = DistributeMoneyRequest(invalidMoney, 3))
            )
        }
    }

    @Test
    fun `addDistribution - INVALID_DISTRIBUTE_DIVISOR`() {
        val invalidDivisor = 0
        assertThrows<InvalidArgumentException> {
            service.addDistribution(
                Distribution.of(userId = 1, roomId = "room", request = DistributeMoneyRequest(100, invalidDivisor))
            )
        }
    }

    @Test
    fun `addDistribution - INVALID_DISTRIBUTE`() {
        assertThrows<InvalidArgumentException> {
            service.addDistribution(
                Distribution.of(userId = 1, roomId = "room", request = DistributeMoneyRequest(money = 10, divisor = 100))
            )
        }
    }

    @Test
    fun `getDistributableMoney`() {
        val senderId = 1
        val roomId = "room"
        val added = added(senderId, roomId, 1000, 3)

        val receiverId = senderId + 1
        val got = service.getDistributableMoney(receiverId, roomId, added.token)

        assertTrue { receiverId == got.receiverId }
    }

    @Test
    fun `getDistributableMoney - NOT_FOUND_DISTRIBUTION`() {
        val senderId = 1
        val roomId = "room1"
        val added = added(senderId, roomId, 1000, 3)

        val receiverId = senderId + 1
        val otherRoomId = roomId.plus("2")
        assertThrows<NotFoundEntityException> {
            service.getDistributableMoney(receiverId, otherRoomId, added.token)
        }
    }

    @Test
    fun `getDistributableMoney - NOT_ALLOWED_TAKE_SELF`() {
        val senderId = 1
        val roomId = "room1"
        val added = added(senderId, roomId, 1000, 3)

        assertThrows<DeniedAccessException> {
            service.getDistributableMoney(senderId, roomId, added.token)
        }
    }

    @Test
    fun `getDistributableMoney - TIMEOUT_TAKE_DISTRIBUTABLE_MONEY`() {
        val senderId = 1
        val roomId = "room1"
        val added = added(senderId, roomId, 1000, 3)

        val fakeCreatedAt = LocalDateTime.now().minusYears(1)
        distributionRepository.save(added.apply { added.createdAt = fakeCreatedAt })

        val receiverId = senderId + 1
        assertThrows<DeniedAccessException> {
            service.getDistributableMoney(receiverId, roomId, added.token)
        }
    }

    @Test
    fun `getDistributableMoney - ALREADY_TAKEN_DISTRIBUTABLE_MONEY`() {
        val senderId = 1
        val roomId = "room1"
        val added = added(senderId, roomId, 1000, 3)

        val receiverId = senderId + 1
        service.getDistributableMoney(receiverId, roomId, added.token)

        assertThrows<DeniedAccessException> {
            service.getDistributableMoney(receiverId, roomId, added.token)
        }
    }

    @Test
    fun `getDistributableMoney - NOT_FOUND_DISTRIBUTABLE_MONEY`() {
        val senderId = 1
        val roomId = "room1"
        val added = added(senderId, roomId, 1000, 1)

        val receiverId = senderId + 1
        service.getDistributableMoney(receiverId, roomId, added.token)

        val otherId = receiverId + 1
        assertThrows<NotFoundEntityException> {
            service.getDistributableMoney(otherId, roomId, added.token)
        }
    }

    @Test
    fun `getDistribution - TIMEOUT_CHECK_DISTRIBUTION`() {
        val senderId = 1
        val roomId = "room1"
        val added = added(senderId, roomId, 1000, 3)

        val fakeCreatedAt = LocalDateTime.now().minusYears(1)
        distributionRepository.save(added.apply { added.createdAt = fakeCreatedAt })

        assertThrows<DeniedAccessException> {
            service.getDistribution(senderId, roomId, added.token)
        }
    }

    private fun added(senderId: Int, roomId: String, money: Int, divisor: Int): Distribution {
        val src = Distribution.of(senderId, roomId, DistributeMoneyRequest(money, divisor))
        return service.addDistribution(src)
    }
}