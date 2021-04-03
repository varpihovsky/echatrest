package com.varpihovsky.echat.rest.model

import com.varpihovsky.echat.rest.controllers.authorize.*

fun AuthorizationRepository.lastIndex() = try {
    this.findAll().last().id + 1
} catch (e: Exception) {
    0
}

fun AuthorizationRepository.isExistsByKey(key: String) = findByKey(key) != null


fun AccountRepository.lastIndex() = try {
    this.findAll().last().id + 1
} catch (e: Exception) {
    0
}

fun AccountRepository.updateAccountAuthorization(id: Long, authorization: Authorization?) {
    findById(id).ifPresent {
        save(it.apply {
            authorizationEntity = authorization
        })
    }
}

fun AccountRepository.removeAuthorization(
    authorizationRepository: AuthorizationRepository,
    account: Account,
    authorization: Authorization
) {
    updateAccountAuthorization(account.id, null)
    authorizationRepository.delete(authorization)
}

fun AccountRepository.findByAuthorizationKey(key: String, authorizationRepository: AuthorizationRepository) =
    authorizationRepository.findByKey(key)?.let { findByAuthorizationEntity(it) }

fun ChatRepository.addChatParticipants(id: Long, vararg participants: Account) {
    findById(id).ifPresent { save(it.apply { chatParticipants.addAll(participants) }) }
}

fun ChatRepository.removeChatParticipants(id: Long, vararg participants: Account) {
    findById(id).ifPresent { save(it.apply { it.chatParticipants.removeAll(participants) }) }
}

fun ChatRepository.addChatAdmins(id: Long, vararg admins: Account) {
    findById(id).ifPresent { save(it.apply { chatAdmins.addAll(admins) }) }
}

fun ChatRepository.removeChatAdmins(id: Long, vararg admins: Account) {
    findById(id).ifPresent { save(it.apply { chatAdmins.removeAll(admins) }) }
}