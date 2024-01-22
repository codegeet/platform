package io.codegeet.sandbox.auth

import io.codegeet.platform.auth.ApiKey
import io.codegeet.platform.auth.ApiKeyRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@DataJpaTest
class ApiKeyRepositoryTest @Autowired constructor(
        val entityManager: TestEntityManager,
        val apiKeyRepository: ApiKeyRepository
) {

    @Test
    fun `create and get token`() {
        val apiKey = ApiKey(UUID.randomUUID().toString(), "", true)
        entityManager.persist(apiKey)
        entityManager.flush()

        val found = apiKeyRepository.findByIdOrNull(apiKey.apiKey)

        // todo assert
    }
}
