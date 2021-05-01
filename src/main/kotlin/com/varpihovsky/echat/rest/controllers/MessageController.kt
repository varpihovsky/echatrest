package com.varpihovsky.echat.rest.controllers

import com.varpihovsky.echat.rest.controllers.response.ResponseList
import com.varpihovsky.echat.rest.model.EchatModel
import com.varpihovsky.echat.rest.model.dao.ChatDAO
import com.varpihovsky.echat.rest.model.dao.MessageDAO
import com.varpihovsky.echat.rest.model.dto.MessageDTO
import com.varpihovsky.echat.rest.model.dto.factory.DTOFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/message")
class MessageController {
    @Autowired
    private lateinit var echatModel: EchatModel

    @Autowired
    private lateinit var dtoFactory: DTOFactory

    @ResponseStatus
    @PostMapping("/write")
    fun writeMessage(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = CHAT_ID_PARAM) chatId: Long,
        @RequestParam(name = TEXT_PARAM) text: String,
        @RequestParam(name = TO_MESSAGE_ID_PARAM, required = false) toId: Long?
    ): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatParticipant(it, chatId)) {
                echatModel.putMessage(
                    it,
                    text,
                    echatModel.getChat(chatId) ?: ChatDAO(),
                    toId?.let { messageId -> echatModel.getMessage(messageId) })
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    @ResponseBody
    @GetMapping("/get-history")
    fun getMessageHistory(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = CHAT_ID_PARAM) chatId: Long
    ): ResponseEntity<ResponseList<MessageDTO>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserIsChatParticipant(it, chatId)) {
                ResponseEntity.ok(
                    ResponseList(echatModel.getMessagesByChat(echatModel.getChat(chatId) ?: ChatDAO())
                        .map<MessageDAO, MessageDTO> { messageDAO -> dtoFactory.createDTO(messageDAO) }
                        .sortedBy { message -> message.created }
                    )
                )
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }

    @ResponseStatus
    @PostMapping("/read")
    fun setMessageRead(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(name = ID_PARAM) messageId: Long
    ): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.getMessage(messageId)?.chatDAO?.id?.let { chatId ->
                    echatModel.isUserIsChatParticipant(it, chatId)
                } == true) {
                try {
                    echatModel.setMessageRead(messageId, it)
                } catch (e: Exception) {
                }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        }

    @ResponseBody
    @GetMapping("/not-read")
    fun getNotReadMessages(
        @RequestParam(name = AUTHORIZATION_KEY_PARAM) key: String
    ): ResponseEntity<ResponseList<MessageDTO>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(
                ResponseList(echatModel.getAllUnreadMessagesByUser(it)
                    .map { messageDAO -> dtoFactory.createDTO(messageDAO) })
            )
        }
}