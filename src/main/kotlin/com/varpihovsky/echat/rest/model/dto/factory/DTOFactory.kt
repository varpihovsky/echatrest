package com.varpihovsky.echat.rest.model.dto.factory

import com.varpihovsky.echat.rest.model.dao.*
import com.varpihovsky.echat.rest.model.dto.*
import org.springframework.stereotype.Component

@Component
class DTOFactory {
    fun <T : DTO> createDTO(dao: DAO, factoryParameter: String? = null): T {
        return when (dao) {
            is AccountDAO ->
                if (factoryParameter == null || factoryParameter == ACCOUNT_WITHOUT_PASSWORD) {
                    createAccountWithoutPasswordDTO(dao)
                } else {
                    createAccountWithPasswordDTO(dao)
                } as? T ?: throwWrongCastException()

            is AuthorizationDAO -> createAuthorizationDTO(dao) as? T ?: throwWrongCastException()
            is ChatDAO -> createChatDTO(dao) as? T ?: throwWrongCastException()
            is MessageDAO -> createMessageDTO(dao) as? T ?: throwWrongCastException()
            is ReadHistoryDAO -> createReadHistoryDTO(dao) as? T ?: throwWrongCastException()
            else -> throw UnsupportedOperationException("This dao doesnt supported.")
        }
    }

    private fun createAccountWithoutPasswordDTO(accountDAO: AccountDAO): AccountWithoutPasswordDTO =
        AccountWithoutPasswordDTO(accountDAO.id, accountDAO.login)

    private fun createAccountWithPasswordDTO(accountDAO: AccountDAO): AccountWithPasswordDTO =
        AccountWithPasswordDTO(
            accountDAO.id,
            accountDAO.login,
            accountDAO.password,
            accountDAO.authorizationDAOEntity?.let { createAuthorizationDTO(it) }
        )

    private fun createAuthorizationDTO(authorizationDAO: AuthorizationDAO): AuthorizationDTO =
        AuthorizationDTO(
            authorizationDAO.id,
            authorizationDAO.key,
            authorizationDAO.created
        )

    private fun createChatDTO(chatDAO: ChatDAO): ChatDTO =
        ChatDTO(
            chatDAO.id,
            chatDAO.name,
            chatDAO.chatAdmins.map { createAccountWithoutPasswordDTO(it) },
            chatDAO.chatParticipants.map { createAccountWithoutPasswordDTO(it) },
            chatDAO.type
        )

    private fun createMessageDTO(messageDAO: MessageDAO): MessageDTO =
        MessageDTO(
            messageDAO.id,
            messageDAO.text,
            createAccountWithoutPasswordDTO(messageDAO.sender),
            createChatDTO(messageDAO.chatDAO),
            messageDAO.receiver?.let { createMessageDTO(it) },
            messageDAO.created
        )

    private fun createReadHistoryDTO(readHistoryDAO: ReadHistoryDAO): ReadHistoryDTO =
        ReadHistoryDTO(
            readHistoryDAO.id,
            createAccountWithoutPasswordDTO(readHistoryDAO.reader),
            createMessageDTO(readHistoryDAO.messageDAO),
            readHistoryDAO.status
        )

    private fun throwWrongCastException(): Nothing =
        throw RuntimeException("This cast doesnt supported. You could write wrong type")

    companion object {
        const val ACCOUNT_WITHOUT_PASSWORD = "account_without_password"
        const val ACCOUNT_WITH_PASSWORD = "account_with_password"
    }
}