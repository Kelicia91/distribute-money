package com.kakaopayhw.distributemoney.controller

import com.kakaopayhw.distributemoney.controller.interfaces.DistributeMoneyRequest
import com.kakaopayhw.distributemoney.controller.interfaces.DistributeMoneyView
import com.kakaopayhw.distributemoney.controller.interfaces.Header
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
        val userId = 123
        val roomId = "room-a"

        val headers = HttpHeaders()
        headers.set(Header.USER_ID, userId.toString())
        headers.set(Header.ROOM_ID, roomId)

        val req = DistributeMoneyRequest(money = 1000, divisor = 3)
        val httpReq = HttpEntity(req, headers)

        val httpRsp = restTemplate.exchange<DistributeMoneyView>(
                "/distribute/money",
                HttpMethod.POST,
                httpReq
        )

        assertEquals(HttpStatus.OK, httpRsp.statusCode)
        val rsp = httpRsp.body
        assertNotNull(rsp)
        assertTrue { rsp!!.token.length == Distribution.TOKEN_LENGTH }
        assertTrue { rsp!!.token.all { char -> Distribution.TOKEN_CHAR_POOL.contains(char) } }
    }
}