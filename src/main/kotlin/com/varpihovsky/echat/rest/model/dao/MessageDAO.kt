package com.varpihovsky.echat.rest.model.dao

import java.util.*
import javax.persistence.*

@Table(name = "messages")
@Entity
data class MessageDAO(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", unique = true, nullable = false)
        var id: Long,

        @Column(name = "text", unique = false, nullable = false, length = 2000)
        var text: String,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_account_sender_id", nullable = false)
        var sender: AccountDAO,

        @ManyToOne
        @JoinColumn(name = "chat_id", nullable = false)
        var chatDAO: ChatDAO,

        @OneToOne
        @JoinColumn(name = "question_id", nullable = true)
        var receiver: MessageDAO?,

        @Column(name = "created", nullable = false)
        @Temporal(TemporalType.TIMESTAMP)
        var created: Date
) : DAO {
    constructor() : this(0, "", AccountDAO(), ChatDAO(), null, Date())
}