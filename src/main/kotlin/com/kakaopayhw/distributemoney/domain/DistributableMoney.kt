package com.kakaopayhw.distributemoney.domain

import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "distributable_money")
data class DistributableMoney (
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "distribution_id")
    var distribution: Distribution,

    @Column(name = "money", nullable = false)
    val money: Int,

    @Column(name = "receiver_id", nullable = true)
    var receiverId: Int? = null
) {
    companion object {
        fun of(distribution: Distribution, money: Int): DistributableMoney {
            return DistributableMoney(
                distribution = distribution,
                money = money
            )
        }
    }

    fun receive(userId: Int): DistributableMoney {
        return this.apply { receiverId = userId }
    }
}