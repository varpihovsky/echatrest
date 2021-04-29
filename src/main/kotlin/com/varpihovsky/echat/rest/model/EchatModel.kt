package com.varpihovsky.echat.rest.model

import com.varpihovsky.echat.rest.model.dao.*
import com.varpihovsky.echat.rest.model.database.*
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

    fun <T> authorizedUserMap(key: String, failedStatus: HttpStatus, block: (AccountDAO) -> ResponseEntity<T>)
            : ResponseEntity<T> {
        return if (authorizationRepository.isExistsByKey(key)) {
            accountRepository.findByAuthorizationKey(key, authorizationRepository)?.let { block.invoke(it) }
                ?: ResponseEntity(failedStatus)
        } else {
            ResponseEntity(failedStatus)
        }
    }

    fun createAuthorization(accountDAO: AccountDAO): AuthorizationDAO {
        val authorization = AuthorizationDAO.generate(accountDAO)
        authorizationRepository.save(authorization)
        accountRepository.updateAccountAuthorization(accountDAO.id, authorization)
        return authorization
    }

    fun removeAuthorization(accountDAO: AccountDAO) {
        accountDAO.authorizationDAOEntity?.let { authorizationRepository.delete(it) }
        accountRepository.updateAccountAuthorization(accountDAO.id, null)
    }

    fun createUser(login: String, password: String) =
        accountRepository.save(AccountDAO(0, login, password.hashCode().toString(), null))


    fun verifyUserExisting(login: String) = accountRepository.findByLogin(login) != null

    fun checkLoginAndPassword(login: String, password: String): Boolean {
        accountRepository.findByLogin(login)?.let {
            return it.login == login && Integer.parseInt(it.password) == password.hashCode()
        }
        return false
    }

    fun getUserByLogin(login: String) = accountRepository.findByLogin(login)

    fun getUserById(id: Long): AccountDAO? {
        accountRepository.findById(id).run {
            return if (isPresent) {
                get()
            } else {
                null
            }
        }
    }

    fun getAllUsers(): List<AccountDAO> =
        accountRepository.findAll().toList()

    fun createChat(creator: AccountDAO, chatName: String, type: ChatDAO.Type): ChatDAO {
        ChatDAO(0, chatName, mutableListOf(creator), mutableListOf(creator), type).let {
            chatRepository.save(it)
            return it
        }
    }

    fun getChat(id: Long): ChatDAO? {
        chatRepository.findById(id).run {
            return if (isPresent) {
                get()
            } else {
                null
            }
        }
    }

    fun getAllChatsByUser(accountDAO: AccountDAO) = chatRepository.findAllByChatParticipants(accountDAO)

    fun isUserIsChatAdmin(accountDAO: AccountDAO, chatId: Long): Boolean =
        getChat(chatId)?.chatAdmins?.contains(accountDAO) == true

    fun isUserIsChatParticipant(accountDAO: AccountDAO, chatId: Long): Boolean =
        getChat(chatId)?.chatParticipants?.contains(accountDAO) == true

    fun addParticipantToChat(accountDAO: AccountDAO, chatId: Long) {
        chatRepository.addChatParticipants(chatId, accountDAO)
    }

    fun removeParticipantFromChat(accountDAO: AccountDAO, chatId: Long) {
        chatRepository.removeChatParticipants(chatId, accountDAO)
    }

    fun addAdminToChat(accountDAO: AccountDAO, chatId: Long) {
        chatRepository.addChatAdmins(chatId, accountDAO)
    }

    fun removeAdminFromChat(accountDAO: AccountDAO, chatId: Long) {
        chatRepository.removeChatAdmins(chatId, accountDAO)
    }

    fun getAllChats(): List<ChatDAO> =
        chatRepository.findAll().toList()


    fun removeChat(id: Long) {
        chatRepository.deleteById(id)
    }

    fun putMessage(from: AccountDAO, text: String, chatDAO: ChatDAO, question: MessageDAO? = null) {
        MessageDAO(0, text, from, chatDAO, question, Date()).let {
            messageRepository.save(it)
            chatDAO.chatParticipants.forEach { participant ->
                if (from == participant)
                    return@forEach
                readHistoryRepository.save(ReadHistoryDAO(0, participant, it, ReadHistoryDAO.Status.NOT_READ))
            }
            readHistoryRepository.save(ReadHistoryDAO(0, from, it, ReadHistoryDAO.Status.READ))
        }
    }

    fun getMessage(id: Long): MessageDAO? {
        messageRepository.findById(id).run {
            return if (isPresent) {
                get()
            } else {
                null
            }
        }
    }

    fun getMessage(messageDAO: MessageDAO) = getMessage(messageDAO.id)

    fun getMessagesByChat(chatDAO: ChatDAO): List<MessageDAO> =
        messageRepository.findAllByChatDAO(chatDAO)

    fun isUserIsMessageAuthor(accountDAO: AccountDAO, messageDAOId: MessageDAO?) =
        messageDAOId?.let { getMessage(it)?.sender } == accountDAO

    fun setMessageRead(messageId: Long, accountDAO: AccountDAO) {
        getMessage(messageId)?.let {
            readHistoryRepository.findByMessageDAOAndReader(it, accountDAO)
                ?.let { readEntity ->
                    readHistoryRepository.save(readEntity.apply { status = ReadHistoryDAO.Status.READ })
                }
        }
    }

    fun getAllUnreadMessagesByUser(accountDAO: AccountDAO): List<MessageDAO> =
        readHistoryRepository.findAllByReaderAndStatus(accountDAO, ReadHistoryDAO.Status.NOT_READ).map { it.messageDAO }
            .sortedBy { it.created }

}