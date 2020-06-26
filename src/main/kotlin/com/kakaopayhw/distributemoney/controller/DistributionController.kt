package com.kakaopayhw.distributemoney.controller

import com.kakaopayhw.distributemoney.controller.interfaces.DistributableMoneyView
import com.kakaopayhw.distributemoney.controller.interfaces.DistributeMoneyRequest
import com.kakaopayhw.distributemoney.controller.interfaces.DistributeMoneyView
import com.kakaopayhw.distributemoney.controller.interfaces.Header
import com.kakaopayhw.distributemoney.domain.Distribution
import com.kakaopayhw.distributemoney.service.DistributionService
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/distribute/money")
class DistributionController(
    private val service: DistributionService
) {
    @PostMapping
    fun distributeMoney(
        @RequestHeader(name = Header.USER_ID) userId: Int,
        @RequestHeader(name = Header.ROOM_ID) roomId: String,
        @RequestBody request: DistributeMoneyRequest
    ): ResponseEntity<DistributeMoneyView> {
        val distribution = service.addDistribution(Distribution.of(userId, roomId, request))
        return ResponseEntity.ok(DistributeMoneyView.of(distribution))
    }

    @PutMapping("/{token}/take")
    fun takeDistributableMoney(
        @RequestHeader(name = Header.USER_ID) userId: Int,
        @RequestHeader(name = Header.ROOM_ID) roomId: String,
        @PathVariable("token") token: String
    ): ResponseEntity<DistributableMoneyView> {
        return try {
            val distributableMoney = service.getDistributableMoney(userId, roomId, token)
            ResponseEntity.ok(DistributableMoneyView.of(distributableMoney))
        } catch(e: OptimisticLockingFailureException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
}