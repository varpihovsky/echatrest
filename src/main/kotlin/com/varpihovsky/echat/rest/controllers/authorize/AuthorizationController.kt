package com.varpihovsky.echat.rest.controllers.authorize

import com.varpihovsky.echat.rest.controllers.AUTHORIZATION_KEY_PARAM
import com.varpihovsky.echat.rest.controllers.LOGIN_PARAM
import com.varpihovsky.echat.rest.controllers.PASSWORD_PARAM
import com.varpihovsky.echat.rest.model.EchatModel
import com.varpihovsky.echat.rest.model.dto.AuthorizationDTO
import com.varpihovsky.echat.rest.model.dto.factory.DTOFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/authorization")
class AuthorizationController {
    @Autowired
    private lateinit var echatModel: EchatModel

    @Autowired
    private lateinit var dtoFactory: DTOFactory

    @ResponseBody
    @GetMapping("/authorize")
    fun authorize(
            @RequestParam(value = LOGIN_PARAM) login: String,
            @RequestParam(value = PASSWORD_PARAM) password: String
    ): ResponseEntity<AuthorizationDTO>? {
        if (echatModel.verifyUserExisting(login) && echatModel.checkLoginAndPassword(login, password)) {
            echatModel.getUserByLogin(login)?.let {
                return ResponseEntity.ok(dtoFactory.createDTO(echatModel.createAuthorization(it)))
            }
        }
        return ResponseEntity<AuthorizationDTO>(HttpStatus.BAD_REQUEST)
    }

    @ResponseStatus
    @PostMapping("/logout")
    fun logout(@RequestParam(value = AUTHORIZATION_KEY_PARAM) key: String): ResponseEntity<Any> =
        echatModel.authorizedUserMap(key, HttpStatus.FORBIDDEN) {
            echatModel.removeAuthorization(it)
            ResponseEntity<Any>(HttpStatus.OK)
        }
}