package com.kakaopayhw.distributemoney.controller

import com.kakaopayhw.distributemoney.controller.interfaces.*
import com.kakaopayhw.distributemoney.domain.DistributableMoneyRepository
import com.kakaopayhw.distributemoney.domain.Distribution
import com.kakaopayhw.distributemoney.domain.DistributionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DistributionControllerTest(
    @Autowired val restTemplate: TestRestTemplate
) {
    @Autowired
    lateinit var distributionRepository: DistributionRepository

    @Autowired
    lateinit var distributableMoneyRepository: DistributableMoneyRepository

    @BeforeEach
    fun beforeEach() {
        distributableMoneyRepository.deleteAll()
        distributionRepository.deleteAll()
    }

    @Test
    fun `POST distribute-money`() {
        val req = DistributeMoneyRequest(money = 1000, divisor = 3)
        val httpReq = HttpEntity(req, getHeader(123, "room-a"))

        val httpRsp = restTemplate.exchange<DistributeMoneyView>("/distribute/money", HttpMethod.POST, httpReq)

        assertEquals(HttpStatus.OK, httpRsp.statusCode)
        val rsp = httpRsp.body
        assertNotNull(rsp)
        assertTrue { rsp!!.token.length == Distribution.TOKEN_LENGTH }
        assertTrue { rsp!!.token.all { char -> Distribution.TOKEN_CHAR_POOL.contains(char) } }
    }

    @Test
    fun `PUT distribute-money-token-take`() {
        val senderId = 123
        val roomId = "room-a"

        val receiverId = senderId + 1
        val token = getToken(senderId, roomId, money = 1000, divisor = 3)
        val httpRsp = restTemplate.exchange<DistributableMoneyView>(
            "/distribute/money/${token}/take",
            HttpMethod.PUT,
            HttpEntity<Void>(getHeader(receiverId, roomId))
        )

        assertEquals(HttpStatus.OK, httpRsp.statusCode)
        val rsp = httpRsp.body
        assertNotNull(rsp)
        assertTrue { rsp!!.money > 0 }
    }

    @Test
    fun `PUT distribute-money-token-take - TIMEOUT_TAKE_DISTRIBUTABLE_MONEY`() {
        val senderId = 1
        val roomId = "room1"

        val token = getToken(senderId, roomId, money = 1000, divisor = 3)

        val added = distributionRepository.findByRoomIdAndToken(roomId, token)
        assertNotNull(added)
        val fakeCreatedAt = LocalDateTime.now().minusYears(1)
        distributionRepository.save(added!!.apply { added.createdAt = fakeCreatedAt })

        val receiverId = senderId + 1
        val httpRsp = restTemplate.exchange<RestExceptionView>(
                "/distribute/money/${token}/take",
                HttpMethod.PUT,
                HttpEntity<Void>(getHeader(receiverId, roomId))
        )

        assertEquals(HttpStatus.FORBIDDEN, httpRsp.statusCode)
    }

    @Test
    fun `GET distribute-money-token`() {
        val senderId = 123
        val roomId = "room-a"
        val money = 1000

        val token = getToken(senderId, roomId, money, divisor = 3)
        val httpRsp = restTemplate.exchange<DistributionView>(
            "/distribute/money/${token}",
            HttpMethod.GET,
            HttpEntity<Void>(getHeader(senderId, roomId))
        )

        assertEquals(HttpStatus.OK, httpRsp.statusCode)
        val rsp = httpRsp.body
        assertNotNull(rsp)
        assertNotNull(rsp!!.distributedAt)
        assertTrue { money == rsp.money }
        assertTrue { rsp.takenMoney == 0 }
        assertTrue { rsp.takens.isEmpty() }
    }

    @Test
    fun `GET distribute-money-token - NOT_FOUND_DISTRIBUTION`() {
        val invalidToken = "unknown-token"
        val httpRsp = restTemplate.exchange<RestExceptionView>(
                "/distribute/money/${invalidToken}",
                HttpMethod.GET,
                HttpEntity<Void>(getHeader(userId = 123, roomId = "room"))
        )
        assertEquals(HttpStatus.BAD_REQUEST, httpRsp.statusCode)
    }

    @Test
    fun `GET distribute-money-token - NOT_ALLOWED_ACCESS_OTHERS_DISTRIBUTION`() {
        val senderId = 123
        val roomId = "room-a"

        val otherId = senderId + 1
        val token = getToken(senderId, roomId, money = 1000, divisor = 3)
        val httpRsp = restTemplate.exchange<RestExceptionView>(
                "/distribute/money/${token}",
                HttpMethod.GET,
                HttpEntity<Void>(getHeader(otherId, roomId))
        )

        assertEquals(HttpStatus.FORBIDDEN, httpRsp.statusCode)
    }

    private fun getHeader(userId: Int, roomId: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.set(Header.USER_ID, userId.toString())
        headers.set(Header.ROOM_ID, roomId)
        return headers
    }

    private fun getToken(senderId: Int, roomId: String, money: Int, divisor: Int): String {
        val distributeMoneyReq = DistributeMoneyRequest(money, divisor)
        val httpDistributeMoneyRsp = restTemplate.exchange<DistributeMoneyView>(
                "/distribute/money",
                HttpMethod.POST,
                HttpEntity(distributeMoneyReq, getHeader(senderId, roomId))
        )
        return httpDistributeMoneyRsp.body!!.token
    }
}