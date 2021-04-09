package com.varpihovsky.echat.rest.model.dao

import javax.persistence.*

@Table(name = "user_account")
@Entity
data class AccountDAO(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", unique = true, nullable = false)
        var id: Long,


        @Column(name = "login", unique = true, nullable = false)
        var login: String,

        @Column(name = "password", nullable = false)
        var password: String,

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "authorization_id", nullable = true)
        var authorizationDAOEntity: AuthorizationDAO?
) : DAO {
    constructor() : this(0, "", "", null)

    fun removeAuthorizationAndPasswordFromEntity() {
        password = ""
        authorizationDAOEntity = null
    }
}