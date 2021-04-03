package com.varpihovsky.echat.rest.controllers.authorize

import com.varpihovsky.echat.rest.model.updateAccountAuthorization
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.persistence.*

@Table(name = "account_authorization")
@Entity
data class Authorization(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    var id: Long,

    @Column(name = "key", nullable = false, unique = true)
    var key: String,

    @Column(name = "created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var created: Date
) {
    constructor() : this(0, "", Date())

    companion object {
        fun create(
            login: String,
            password: String,
            accountRepository: AccountRepository,
            authorizationRepository: AuthorizationRepository,
            account: Account
        ) = generate(login, password).let {
            authorizationRepository.save(it)
            accountRepository.updateAccountAuthorization(account.id, it)
            it
        }

        fun generate(account: Account) = generate(account.login, account.password)

        fun generate(login: String, password: String): Authorization {
            generatePrivateKey().let {
                return Authorization(
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

@Table(name = "user_account")
@Entity
data class Account(
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
    var authorizationEntity: Authorization?
) {
    constructor() : this(0, "", "", null)

    fun hideAuthorizationAndPassword(): Account {
        return Account(id, login, "", null)
    }

    fun removeAuthorizationAndPasswordFromEntity() {
        password = ""
        authorizationEntity = null
    }
}

@Table(name = "chats")
@Entity
data class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    var id: Long,

    @Column(name = "name", unique = false, nullable = false)
    var name: String,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_admins_id", nullable = false)
    var chatAdmins: MutableList<Account>,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_participant_id", nullable = false)
    var chatParticipants: MutableList<Account>

) {
    constructor() : this(0, "", mutableListOf(), mutableListOf())
}

@Table(name = "messages")
@Entity
data class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    var id: Long,

    @Column(name = "text", unique = false, nullable = false, length = 2000)
    var text: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_sender_id", nullable = false)
    var sender: Account,

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    var chat: Chat,

    @OneToOne
    @JoinColumn(name = "question_id", nullable = true)
    var receiver: Message?,

    @Column(name = "created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var created: Date
) {
    constructor() : this(0, "", Account(), Chat(), null, Date())
}

@Table(name = "read_history")
@Entity
data class ReadEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    var id: Long,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    var reader: Account,

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    var message: Message,

    @Column(name = "status")
    var status: Status
) {
    constructor() : this(0, Account(), Message(), Status.NOT_READ)

    enum class Status {
        READ,
        NOT_READ
    }
}