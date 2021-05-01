package com.varpihovsky.echat.rest.model.dao

import javax.persistence.*

@Table(name = "invites")
@Entity
data class InviteDAO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", nullable = false)
    val account: AccountDAO,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: ChatDAO
) : DAO {
    constructor() : this(0, AccountDAO(), ChatDAO())
}