package com.varpihovsky.echat.rest.model.dao

import javax.persistence.*

@Table(name = "chats")
@Entity
data class ChatDAO(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", unique = true, nullable = false)
        var id: Long,

        @Column(name = "name", unique = false, nullable = false)
        var name: String,

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_account_admins_id", nullable = false)
        var chatAdmins: MutableList<AccountDAO>,

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_account_participant_id", nullable = false)
        var chatParticipants: MutableList<AccountDAO>

) : DAO {
    constructor() : this(0, "", mutableListOf(), mutableListOf())
}