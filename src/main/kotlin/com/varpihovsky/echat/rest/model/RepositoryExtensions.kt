package com.varpihovsky.echat.rest.model

import com.varpihovsky.echat.rest.model.dao.AccountDAO
import com.varpihovsky.echat.rest.model.dao.AuthorizationDAO
import com.varpihovsky.echat.rest.model.database.AccountRepository
import com.varpihovsky.echat.rest.model.database.AuthorizationRepository
import com.varpihovsky.echat.rest.model.database.ChatRepository

fun AuthorizationRepository.isExistsByKey(key: String) = findByKey(key) != null

fun AccountRepository.updateAccountAuthorization(id: Long, authorizationDAO: AuthorizationDAO?) {
    findById(id).ifPresent {
        save(it.apply {
            authorizationDAOEntity = authorizationDAO
        })
    }
}

fun AccountRepository.removeAuthorization(
        authorizationRepository: AuthorizationRepository,
        accountDAO: AccountDAO,
        authorizationDAO: AuthorizationDAO
) {
    updateAccountAuthorization(accountDAO.id, null)
    authorizationRepository.delete(authorizationDAO)
}

fun AccountRepository.findByAuthorizationKey(key: String, authorizationRepository: AuthorizationRepository) =
        authorizationRepository.findByKey(key)?.let { findByAuthorizationDAOEntity(it) }

fun ChatRepository.addChatParticipants(id: Long, vararg participants: AccountDAO) {
    findById(id).ifPresent { save(it.apply { chatParticipants.addAll(participants) }) }
}

fun ChatRepository.removeChatParticipants(id: Long, vararg participants: AccountDAO) {
    findById(id).ifPresent { save(it.apply { it.chatParticipants.removeAll(participants) }) }
}

fun ChatRepository.addChatAdmins(id: Long, vararg admins: AccountDAO) {
    findById(id).ifPresent { save(it.apply { chatAdmins.addAll(admins) }) }
}

fun ChatRepository.removeChatAdmins(id: Long, vararg admins: AccountDAO) {
    findById(id).ifPresent { save(it.apply { chatAdmins.removeAll(admins) }) }
}