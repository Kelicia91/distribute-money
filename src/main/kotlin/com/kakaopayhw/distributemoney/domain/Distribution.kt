package com.kakaopayhw.distributemoney.domain

import com.kakaopayhw.distributemoney.controller.interfaces.DistributeMoneyRequest
import org.hibernate.annotations.CreationTimestamp
import java.security.SecureRandom
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "distribution")
data class Distribution (
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @Column(name = "sender_id", nullable = false)
    val senderId: Int,

    @Column(name = "room_id", nullable = false)
    val roomId: String,

    @Column(name = "token", nullable = false, length = 3)
    val token: String,

    @Column(name = "money", nullable = false)
    val money: Int,

    @Column(name = "divisor", nullable = false)
    val divisor: Int,

    @OneToMany(mappedBy = "distribution", cascade = [CascadeType.ALL])
    var distributableMoneys: List<DistributableMoney> = mutableListOf()
) {
    companion object {
        const val TOKEN_LENGTH = 3

        val TOKEN_CHAR_POOL: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        fun of(userId: Int, roomId: String, request: DistributeMoneyRequest): Distribution {
            return Distribution(
                senderId = userId,
                roomId = roomId,
                token = generateToken(),
                money = request.money,
                divisor = request.divisor
            )
        }

        private fun generateToken(): String {
            // @NOTE: https://www.baeldung.com/kotlin-random-number
            val random = SecureRandom()
            return (1..TOKEN_LENGTH)
                    .map { random.nextInt(TOKEN_CHAR_POOL.size) }
                    .map(TOKEN_CHAR_POOL::get)
                    .joinToString("")
        }
    }

    fun isExpired(): Boolean {
        if (createdAt == null) { return true }
        return createdAt!!.plusMinutes(10) < LocalDateTime.now()
    }
}