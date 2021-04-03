package com.varpihovsky.echat.rest.controllers.profile

import com.varpihovsky.echat.rest.controllers.AUTHORIZATION_KEY_PARAM
import com.varpihovsky.echat.rest.controllers.ID_PARAM
import com.varpihovsky.echat.rest.controllers.LOGIN_PARAM
import com.varpihovsky.echat.rest.controllers.PASSWORD_PARAM
import com.varpihovsky.echat.rest.controllers.authorize.Account
import com.varpihovsky.echat.rest.model.EchatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/profile")
class ProfileController {
    @Autowired
    private lateinit var echatModel: EchatModel

    @ResponseStatus
    @PostMapping("/register")
    fun register(
        @RequestParam(value = LOGIN_PARAM) login: String,
        @RequestParam(value = PASSWORD_PARAM) password: String
    ): ResponseEntity<String> {
        return if (!echatModel.verifyUserExisting(login) && password.length >= 8) {
            echatModel.createUser(login, password)
            ResponseEntity(REGISTRATION_SUCCESS, HttpStatus.ACCEPTED)
        } else {
            ResponseEntity(REGISTRATION_FAILURE, HttpStatus.BAD_REQUEST)
        }
    }

    @ResponseBody
    @GetMapping("/get/by-key")
    fun profileByKey(@RequestParam(value = AUTHORIZATION_KEY_PARAM) key: String): ResponseEntity<Account> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(it)
        }


    @ResponseBody
    @GetMapping("/get/by-id")
    fun profileById(
        @RequestParam(value = AUTHORIZATION_KEY_PARAM) key: String,
        @RequestParam(value = ID_PARAM) id: Long
    ): ResponseEntity<Account> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(echatModel.getUserById(id)?.hideAuthorizationAndPassword())
        }


    @ResponseBody
    @GetMapping("/get/all")
    fun allProfiles(@RequestParam(value = AUTHORIZATION_KEY_PARAM) key: String): ResponseEntity<List<Account>> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            ResponseEntity.ok(echatModel.getAllUsers().map { it.hideAuthorizationAndPassword() })
        }


    companion object {
        private const val REGISTRATION_SUCCESS = "User registered successfully"
        private const val REGISTRATION_FAILURE = "User with this login is exists or password length less than 8"
    }
}