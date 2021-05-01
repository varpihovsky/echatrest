package com.varpihovsky.echat.rest.controllers

import com.varpihovsky.echat.rest.controllers.response.ResponseList
import com.varpihovsky.echat.rest.model.EchatModel
import com.varpihovsky.echat.rest.model.dto.InviteDTO
import com.varpihovsky.echat.rest.model.dto.factory.DTOFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/invite")
class InviteController {
    @Autowired
    private lateinit var echatModel: EchatModel

    @Autowired
    private lateinit var dtoFactory: DTOFactory

    @PostMapping
    @ResponseStatus
    fun invite(
        @RequestParam(AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(CHAT_ID_PARAM) chatId: Long,
        @RequestParam(ID_PARAM) userId: Long
    ): ResponseEntity<Any> = echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
        if (echatModel.isUserIsChatAdmin(it, chatId) && echatModel.getUserById(userId) != null) {
            echatModel.createInvite(chatId, userId)
            ResponseEntity.ok().build()
        } else {
            ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
        }
    }

    @GetMapping("/get/all")
    @ResponseBody
    fun getInvites(@RequestParam(AUTHORIZATION_KEY_PARAM) key: String): ResponseEntity<ResponseList<InviteDTO>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(
                ResponseList(
                    echatModel.getInvitesByUser(it).map { invite -> dtoFactory.createDTO(invite) })
            )
        }

    @PostMapping("/accept")
    @ResponseStatus
    fun acceptInvite(
        @RequestParam(AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(ID_PARAM) inviteId: Long
    ): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserAssignedToInvite(it, inviteId)) {
                echatModel.getInviteById(inviteId)?.let { invite ->
                    echatModel.removeInvite(invite)
                    echatModel.addParticipantToChat(it, invite.chat.id)
                }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
            }
        }

    @PostMapping("/decline")
    @ResponseStatus
    fun declineInvite(
        @RequestParam(AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(ID_PARAM) inviteId: Long
    ): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            if (echatModel.isUserAssignedToInvite(it, inviteId)) {
                echatModel.removeInviteById(inviteId)
                ResponseEntity.ok().build()
            } else {
                ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
            }
        }
}