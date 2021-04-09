package com.varpihovsky.echat.rest.model.dao

import javax.persistence.*

@Table(name = "read_history")
@Entity
data class ReadHistoryDAO(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", unique = true, nullable = false)
        var id: Long,

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "account_id", nullable = false)
        var reader: AccountDAO,

        @OneToOne
        @JoinColumn(name = "message_id", nullable = false)
        var messageDAO: MessageDAO,

        @Column(name = "status")
        var status: Status
) : DAO {
    constructor() : this(0, AccountDAO(), MessageDAO(), Status.NOT_READ)

    enum class Status {
        READ,
        NOT_READ
    }
}