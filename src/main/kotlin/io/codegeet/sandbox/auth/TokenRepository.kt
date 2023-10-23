package io.codegeet.sandbox.auth

import org.springframework.data.repository.CrudRepository
import java.util.*

interface TokenRepository : CrudRepository<Token, UUID> {

}
