package io.codegeet.sandbox.auth

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@DataJpaTest
class TokenRepositoryTest @Autowired constructor(
    val entityManager: TestEntityManager,
    val tokenRepository: TokenRepository
) {

    @Test
    fun `create and get token`() {
        val token = Token(UUID.randomUUID())
        entityManager.persist(token)
        entityManager.flush()

        val found = tokenRepository.findByIdOrNull(token.uuid)

        // todo assert
    }
}
