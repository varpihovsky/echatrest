package com.varpihovsky.echat.rest.controllers

import com.varpihovsky.echat.rest.controllers.response.ResponseList
import com.varpihovsky.echat.rest.model.EchatModel
import com.varpihovsky.echat.rest.model.dao.ChatDAO
import com.varpihovsky.echat.rest.model.dto.ChatDTO
import com.varpihovsky.echat.rest.model.dto.factory.DTOFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController {
    @Autowired
    private lateinit var echatModel: EchatModel

    @Autowired
    private lateinit var dtoFactory: DTOFactory

    @ResponseBody
    @PostMapping("/create")
    fun createChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = CHAT_NAME_PARAM) name: String,
        @RequestParam(name = "type", required = false) type: String?
    ): ResponseEntity<ChatDTO> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            val typeDAO = when (type) {
                "open" -> ChatDAO.Type.OPEN
                "closed" -> ChatDAO.Type.CLOSED
                else -> ChatDAO.Type.OPEN
            }
            ResponseEntity.ok(dtoFactory.createDTO(echatModel.createChat(it, name, typeDAO)))
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
    @GetMapping("/get/all")
    fun getAllChats(@RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String): ResponseEntity<ResponseList<ChatDTO>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(ResponseList(echatModel.getAllChats().map<ChatDAO, ChatDTO> { dtoFactory.createDTO(it) }
                .filter { it.type == ChatDAO.Type.OPEN }))
        }

    @ResponseBody
    @GetMapping("/get/by-name")
    fun getChatsByName(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = "name") name: String
    ): ResponseEntity<ResponseList<ChatDTO>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(ResponseList(echatModel.getAllChats().map<ChatDAO, ChatDTO> { dtoFactory.createDTO(it) }
                .filter { it.type == ChatDAO.Type.OPEN }))
        }

    @ResponseBody
    @GetMapping("get/by-participant")
    fun getUserChats(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = ID_PARAM) id: Long
    ) =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(
                ResponseList(
                    echatModel.getAllChatsByUser(it).map<ChatDAO, ChatDTO> { dtoFactory.createDTO(it) }
                        .filter { it.type == ChatDAO.Type.OPEN })
            )
        }

    @ResponseStatus
    @PostMapping("/remove/participant")
    fun removeParticipantFromChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = CHAT_ID_PARAM) chatId: Long,
        @RequestParam(name = LOGIN_PARAM, required = false) login: String?,
        @RequestParam(name = ID_PARAM, required = false) participantId: Long?
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
        @RequestParam(name = CHAT_ID_PARAM) chatId: Long,
        @RequestParam(name = LOGIN_PARAM, required = false) login: String?,
        @RequestParam(name = ID_PARAM, required = false) adminId: Long?
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
                echatModel.getUserByLogin(login)?.let { toAdd ->
                    if (echatModel.isUserIsChatParticipant(toAdd, chatId)) {
                        echatModel.addAdminToChat(toAdd, chatId)
                    } else {
                        return@authorizedUserMap ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
                    }
                }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    fun addAdminToChatById(key: String, chatId: Long, adminId: Long): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatAdmin(it, chatId)) {
                echatModel.getUserById(adminId)?.let { toAdd ->
                    if (echatModel.isUserIsChatParticipant(toAdd, chatId)) {
                        echatModel.addAdminToChat(toAdd, chatId)
                    } else {
                        return@authorizedUserMap ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
                    }
                }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    @ResponseStatus
    @PostMapping("/remove/admin")
    fun removeAdminFromChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = CHAT_ID_PARAM) chatId: Long,
        @RequestParam(name = LOGIN_PARAM, required = false) login: String?,
        @RequestParam(name = ID_PARAM, required = false) adminId: Long?
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
    @PostMapping("/join")
    fun joinToChat(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = ID_PARAM) chatId: Long
    ): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.getChat(chatId)?.type == ChatDAO.Type.OPEN) {
                echatModel.addParticipantToChat(it, chatId)
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
            }
        }
}
