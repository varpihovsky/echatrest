package com.varpihovsky.echat.rest.model

import com.varpihovsky.echat.rest.controllers.authorize.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.util.*

@Component
class EchatModel {

    @Autowired
    private lateinit var authorizationRepository: AuthorizationRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Autowired
    private lateinit var readHistoryRepository: ReadHistoryRepository

    fun <T> authorizedUserMap(key: String, failedStatus: HttpStatus, block: (Account) -> ResponseEntity<T>)
            : ResponseEntity<T> {
        return if (authorizationRepository.isExistsByKey(key)) {
            accountRepository.findByAuthorizationKey(key, authorizationRepository)?.let { block.invoke(it) }
                ?: ResponseEntity(failedStatus)
        } else {
            ResponseEntity(failedStatus)
        }
    }

    fun createAuthorization(account: Account): Authorization {
        val authorization = Authorization.generate(account)
        authorizationRepository.save(authorization)
        accountRepository.updateAccountAuthorization(account.id, authorization)
        return authorization
    }

    fun removeAuthorization(account: Account) {
        account.authorizationEntity?.let { authorizationRepository.delete(it) }
        accountRepository.updateAccountAuthorization(account.id, null)
    }

    fun createUser(login: String, password: String) {
        accountRepository.save(Account(0, login, password.hashCode().toString(), null))
    }

    fun verifyUserExisting(login: String) = accountRepository.findByLogin(login) != null

    fun checkLoginAndPassword(login: String, password: String): Boolean {
        accountRepository.findByLogin(login)?.let {
            return it.login == login && Integer.parseInt(it.password) == password.hashCode()
        }
        return false
    }

    fun getUserByLogin(login: String) = accountRepository.findByLogin(login)

    fun getUserById(id: Long): Account? {
        accountRepository.findById(id).run {
            return if (isPresent) {
                get()
            } else {
                null
            }
        }
    }

    fun getAllUsers(): List<Account> =
        accountRepository.findAll().toList()

    fun createChat(creator: Account, chatName: String): Chat {
        Chat(0, chatName, mutableListOf(creator), mutableListOf(creator)).let {
            chatRepository.save(it)
            return it
        }
    }

    fun getChat(id: Long): Chat? {
        chatRepository.findById(id).run {
            return if (isPresent) {
                get()
            } else {
                null
            }
        }
    }

    fun isUserIsChatAdmin(account: Account, chatId: Long): Boolean =
        getChat(chatId)?.chatAdmins?.contains(account) == true

    fun isUserIsChatParticipant(account: Account, chatId: Long): Boolean =
        getChat(chatId)?.chatParticipants?.contains(account) == true

    fun addParticipantToChat(account: Account, chatId: Long) {
        chatRepository.addChatParticipants(chatId, account)
    }

    fun removeParticipantFromChat(account: Account, chatId: Long) {
        chatRepository.removeChatParticipants(chatId, account)
    }

    fun addAdminToChat(account: Account, chatId: Long) {
        chatRepository.addChatAdmins(chatId, account)
    }

    fun removeAdminFromChat(account: Account, chatId: Long) {
        chatRepository.removeChatAdmins(chatId, account)
    }

    fun getAllChats(): List<Chat> =
        chatRepository.findAll().toList()


    fun removeChat(id: Long) {
        chatRepository.deleteById(id)
    }

    fun putMessage(from: Account, text: String, chat: Chat, question: Message? = null) {
        Message(0, text, from, chat, question, Date()).let {
            messageRepository.save(it)
            chat.chatParticipants.forEach { participant ->
                if (from == participant)
                    return@forEach
                readHistoryRepository.save(ReadEntity(0, participant, it, ReadEntity.Status.NOT_READ))
            }
            readHistoryRepository.save(ReadEntity(0, from, it, ReadEntity.Status.READ))
        }
    }

    fun getMessage(id: Long): Message? {
        messageRepository.findById(id).run {
            return if (isPresent) {
                get()
            } else {
                null
            }
        }
    }

    fun getMessage(message: Message) = getMessage(message.id)

    fun getMessagesByChat(chat: Chat): List<Message> =
        messageRepository.findAllByChat(chat)

    fun isUserIsMessageAuthor(account: Account, messageId: Message?) =
        messageId?.let { getMessage(it)?.sender } == account

    fun setMessageRead(messageId: Long) {
        getMessage(messageId)?.let {
            readHistoryRepository.findByMessage(it)
                ?.let { readEntity -> readHistoryRepository.save(readEntity.apply { status = ReadEntity.Status.READ }) }
        }
    }

    fun getAllUnreadMessagesByUser(account: Account): List<Message> =
        readHistoryRepository.findAllByReaderAndStatus(account, ReadEntity.Status.NOT_READ).map {
            var message = it.message
            message.sender.removeAuthorizationAndPasswordFromEntity()
            while (true) {
                var receiver = message.receiver
                receiver?.sender?.removeAuthorizationAndPasswordFromEntity()
                if (receiver == null) break
            }

            message.chat.apply {
                chatParticipants.onEach { participant -> participant.removeAuthorizationAndPasswordFromEntity() }
                chatAdmins.onEach { admin -> admin.removeAuthorizationAndPasswordFromEntity() }
            }

            message
        }.sortedBy { it.created }

}