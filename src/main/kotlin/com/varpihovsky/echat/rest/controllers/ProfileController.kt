package com.varpihovsky.echat.rest.controllers

import com.varpihovsky.echat.rest.controllers.response.ResponseList
import com.varpihovsky.echat.rest.model.EchatModel
import com.varpihovsky.echat.rest.model.dto.AccountWithPasswordDTO
import com.varpihovsky.echat.rest.model.dto.AccountWithoutPasswordDTO
import com.varpihovsky.echat.rest.model.dto.factory.DTOFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/profile")
class ProfileController {
    @Autowired
    private lateinit var echatModel: EchatModel

    @Autowired
    private lateinit var dtoFactory: DTOFactory

    @ResponseStatus
    @PostMapping("/register")
    fun register(
        @RequestParam(value = LOGIN_PARAM) login: String,
        @RequestParam(value = PASSWORD_PARAM) password: String
    ): ResponseEntity<AccountWithPasswordDTO> {
        return if (!echatModel.verifyUserExisting(login) && password.length >= 8) {
            ResponseEntity.accepted()
                .body(
                    dtoFactory.createDTO(
                        echatModel.createUser(login, password),
                        DTOFactory.ACCOUNT_WITH_PASSWORD
                    ) as AccountWithPasswordDTO
                )
        } else {
            ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
        }
    }

    @ResponseBody
    @GetMapping("/get/by-key")
    fun profileByKey(@RequestParam(value = AUTHORIZATION_KEY_PARAM) key: String): ResponseEntity<AccountWithPasswordDTO> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(dtoFactory.createDTO(it, DTOFactory.ACCOUNT_WITH_PASSWORD))
        }


    @ResponseBody
    @GetMapping("/get/by-id")
    fun profileById(
        @RequestParam(value = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(value = ID_PARAM) id: Long
    ): ResponseEntity<AccountWithoutPasswordDTO?> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            val user = echatModel.getUserById(id) ?: return@authorizedUserMap ResponseEntity(HttpStatus.NOT_FOUND)
            ResponseEntity.ok(user.let { dtoFactory.createDTO(it) })
        }


    @ResponseBody
    @GetMapping("/get/all")
    fun allProfiles(@RequestParam(value = AUTHORIZATION_KEY_PARAM) key: String): ResponseEntity<ResponseList<AccountWithoutPasswordDTO>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(ResponseList(echatModel.getAllUsers().map { dtoFactory.createDTO(it) }))
        }

    @ResponseBody
    @GetMapping("/get/by-name")
    fun profileByName(
        @RequestParam(value = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(value = "name") name: String
    ): ResponseEntity<ResponseList<AccountWithoutPasswordDTO>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(ResponseList(echatModel.getAllUsers().filter { it.login.contains(name, true) }
                .map { dtoFactory.createDTO(it) }))
        }
}