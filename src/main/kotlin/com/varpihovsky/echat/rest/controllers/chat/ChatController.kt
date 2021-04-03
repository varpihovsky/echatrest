package com.varpihovsky.echat.rest.controllers.chat

import com.varpihovsky.echat.rest.controllers.AUTHORIZATION_KEY_PARAM
import com.varpihovsky.echat.rest.controllers.CHAT_NAME_PARAM
import com.varpihovsky.echat.rest.controllers.ID_PARAM
import com.varpihovsky.echat.rest.controllers.LOGIN_PARAM
import com.varpihovsky.echat.rest.controllers.authorize.Chat
import com.varpihovsky.echat.rest.controllers.authorize.Message
import com.varpihovsky.echat.rest.model.EchatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController {
    @Autowired
    private lateinit var echatModel: EchatModel

    @ResponseBody
    @PostMapping("/create")
    fun createChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = CHAT_NAME_PARAM) name: String
    ): ResponseEntity<Chat> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(echatModel.createChat(it, name).apply {
                chatAdmins.onEach { admin -> admin.removeAuthorizationAndPasswordFromEntity() }
                chatParticipants.onEach { participant -> participant.removeAuthorizationAndPasswordFromEntity() }
            })
        }

    @ResponseStatus
    @PostMapping("/remove")
    fun removeChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = ID_PARAM) id: Long
    ): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatAdmin(it, id)) {
                echatModel.removeChat(id)
                ResponseEntity.ok().build()
            } else {
                ResponseEntity.badRequest().build()
            }
        }

    @ResponseBody
    @GetMapping("get-all")
    fun getAllChats(@RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String): ResponseEntity<List<Chat>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(echatModel.getAllChats().map { chat ->
                chat.chatParticipants.onEach { participant -> participant.removeAuthorizationAndPasswordFromEntity() }
                chat.chatAdmins.onEach { admin -> admin.removeAuthorizationAndPasswordFromEntity() }
                chat
            })
        }

    @ResponseStatus
    @PostMapping("/add/participant")
    fun addParticipantToChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = "chat-id") chatId: Long,
        @RequestParam(name = LOGIN_PARAM, required = false) login: String?,
        @RequestParam(name = "id", required = false) participantId: Long?
    ): ResponseEntity<Any> =
        if (login != null && participantId == null) {
            addParticipantToChatByLogin(key, chatId, login)
        } else if (login == null && participantId != null) {
            addParticipantToChatById(key, chatId, participantId)
        } else {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }

    private fun addParticipantToChatByLogin(key: String, chatId: Long, login: String): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatParticipant(it, chatId)) {
                echatModel.getUserByLogin(login)?.let { user -> echatModel.addParticipantToChat(user, chatId) }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    private fun addParticipantToChatById(key: String, chatId: Long, participantId: Long): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatParticipant(it, chatId)) {
                echatModel.getUserById(participantId)?.let { user -> echatModel.addParticipantToChat(user, chatId) }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    @ResponseStatus
    @PostMapping("/remove/participant")
    fun removeParticipantFromChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = "chat-id") chatId: Long,
        @RequestParam(name = LOGIN_PARAM, required = false) login: String?,
        @RequestParam(name = "id", required = false) participantId: Long?
    ): ResponseEntity<Any> =
        if (login != null && participantId == null) {
            removeParticipantFromChatByLogin(key, chatId, login)
        } else if (login == null && participantId != null) {
            removeParticipantFromChatById(key, chatId, participantId)
        } else {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }

    private fun removeParticipantFromChatByLogin(key: String, chatId: Long, login: String): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatParticipant(it, chatId)) {
                echatModel.getUserByLogin(login)
                    ?.let { toRemove -> echatModel.removeParticipantFromChat(toRemove, chatId) }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }


    private fun removeParticipantFromChatById(key: String, chatId: Long, participantId: Long): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatParticipant(it, chatId)) {
                echatModel.getUserById(participantId)
                    ?.let { toRemove -> echatModel.removeParticipantFromChat(toRemove, chatId) }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    @ResponseStatus
    @PostMapping("/add/admin")
    fun addAdminToChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = "chat-id") chatId: Long,
        @RequestParam(name = LOGIN_PARAM, required = false) login: String?,
        @RequestParam(name = "adminId", required = false) adminId: Long?
    ): ResponseEntity<Any> =
        if (login != null && adminId == null) {
            addAdminToChatByLogin(key, chatId, login)
        } else if (login == null && adminId != null) {
            addAdminToChatById(key, chatId, adminId)
        } else {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }

    fun addAdminToChatByLogin(key: String, chatId: Long, login: String): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatAdmin(it, chatId)) {
                echatModel.getUserByLogin(login)?.let { toAdd -> echatModel.addAdminToChat(toAdd, chatId) }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    fun addAdminToChatById(key: String, chatId: Long, adminId: Long): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatAdmin(it, chatId)) {
                echatModel.getUserById(adminId)?.let { toAdd -> echatModel.addAdminToChat(toAdd, chatId) }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    @ResponseStatus
    @PostMapping("/remove/admin")
    fun removeAdminFromChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = "chat-id") chatId: Long,
        @RequestParam(name = LOGIN_PARAM, required = false) login: String?,
        @RequestParam(name = "id", required = false) adminId: Long
    ): ResponseEntity<Any> =
        if (login != null && adminId == null) {
            removeAdminFromChatByLogin(key, chatId, login)
        } else if (login == null && adminId != null) {
            removeAdminFromChatById(key, chatId, adminId)
        } else {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }


    private fun removeAdminFromChatByLogin(key: String, chatId: Long, login: String): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatAdmin(it, chatId)) {
                echatModel.getUserByLogin(login)?.let { toRemove -> echatModel.removeAdminFromChat(toRemove, chatId) }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    private fun removeAdminFromChatById(key: String, chatId: Long, adminId: Long): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatAdmin(it, chatId)) {
                echatModel.getUserById(adminId)?.let { toRemove -> echatModel.removeAdminFromChat(toRemove, chatId) }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }

        }

    @ResponseStatus
    @PostMapping("/message/write")
    fun writeMessage(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = "chat-id") chatId: Long,
        @RequestParam(name = "text") text: String,
        @RequestParam(name = "to-message-id", required = false) toId: Long?
    ): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatParticipant(it, chatId)) {
                echatModel.putMessage(
                    it,
                    text,
                    echatModel.getChat(chatId) ?: Chat(),
                    toId?.let { messageId -> echatModel.getMessage(messageId) })
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    @ResponseBody
    @GetMapping("/message/get-history")
    fun getMessageHistory(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = "chat-id") chatId: Long
    ): ResponseEntity<List<Message>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatParticipant(it, chatId)) {
                ResponseEntity.ok(
                    echatModel.getMessagesByChat(echatModel.getChat(chatId) ?: Chat())
                        .sortedBy { message -> message.created })
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    @ResponseStatus
    @PostMapping("/message/read")
    fun setMessageRead(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = "message-id") messageId: Long
    ): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsMessageAuthor(it, echatModel.getMessage(messageId))) {
                echatModel.setMessageRead(messageId)
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        }

    @ResponseBody
    @PostMapping("/message/not-read")
    fun getNotReadMessages(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String
    ): ResponseEntity<List<Message>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(echatModel.getAllUnreadMessagesByUser(it))
        }
}
