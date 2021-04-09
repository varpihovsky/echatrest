package com.varpihovsky.echat.rest.model.dao

import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.persistence.*

@Table(name = "account_authorization")
@Entity
data class AuthorizationDAO(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false, unique = true)
        var id: Long,

        @Column(name = "key", nullable = false, unique = true)
        var key: String,

        @Column(name = "created", nullable = false)
        @Temporal(TemporalType.TIMESTAMP)
        var created: Date
) : DAO {
    constructor() : this(0, "", Date())

    companion object {
        fun generate(accountDAO: AccountDAO) = generate(accountDAO.login, accountDAO.password)


        fun generate(login: String, password: String): AuthorizationDAO {
            generatePrivateKey().let {
                return AuthorizationDAO(
                        0,
                        generatePublicKey(it, login + password.hashCode() + System.currentTimeMillis()),
                        Date()
                )
            }
        }

        private fun generatePublicKey(privateKey: SecretKey, data: String) =
                Cipher.getInstance("AES").let { cipher ->
                    cipher.init(Cipher.ENCRYPT_MODE, privateKey)
                    cipher.doFinal(data.toByteArray()).toString()
                }

        private fun generatePrivateKey() =
                KeyGenerator.getInstance("AES").let {
                    it.init(256)
                    it.generateKey()
                }

    }
}