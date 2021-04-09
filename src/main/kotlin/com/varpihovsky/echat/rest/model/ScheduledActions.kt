package com.varpihovsky.echat.rest.model

import com.varpihovsky.echat.rest.model.database.AccountRepository
import com.varpihovsky.echat.rest.model.database.AuthorizationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.util.*

@Configuration
@EnableScheduling
class ScheduledActions {
    @Autowired
    private lateinit var authorizationRepository: AuthorizationRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Scheduled(fixedDelay = EIGHT_HOURS)
    fun clearAuthorizations() {
        authorizationRepository.findAll().forEach {
            if (Date().time - it.created.time > EIGHT_HOURS) {
                accountRepository.findByAuthorizationDAOEntity(it)?.let { it1 ->
                    accountRepository.removeAuthorization(
                            authorizationRepository,
                            it1, it
                    )
                }
            }
        }
    }

    companion object {
        private const val EIGHT_HOURS = 2800000L //in milliseconds
    }
}