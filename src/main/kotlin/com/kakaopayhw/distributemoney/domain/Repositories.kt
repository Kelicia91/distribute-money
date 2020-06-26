package com.kakaopayhw.distributemoney.domain

import org.springframework.data.jpa.repository.JpaRepository

interface DistributableMoneyRepository : JpaRepository<DistributableMoney, Long>

interface DistributionRepository : JpaRepository<Distribution, Long> {
    fun findByRoomIdAndToken(roomId: String, token: String): Distribution?
}