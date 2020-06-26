package com.kakaopayhw.distributemoney.controller

import com.kakaopayhw.distributemoney.controller.interfaces.DistributeMoneyRequest
import com.kakaopayhw.distributemoney.controller.interfaces.DistributeMoneyView
import com.kakaopayhw.distributemoney.controller.interfaces.Header
import com.kakaopayhw.distributemoney.domain.Distribution
import com.kakaopayhw.distributemoney.service.DistributionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class DistributionController(
    private val service: DistributionService
) {
    @PostMapping("/distribute/money")
    fun distributeMoney(
        @RequestHeader(name = Header.USER_ID) userId: Int,
        @RequestHeader(name = Header.ROOM_ID) roomId: String,
        @RequestBody request: DistributeMoneyRequest
    ): ResponseEntity<DistributeMoneyView> {
        val distribution = service.addDistribution(Distribution.of(userId, roomId, request))
        return ResponseEntity.ok(DistributeMoneyView.of(distribution))
    }
}