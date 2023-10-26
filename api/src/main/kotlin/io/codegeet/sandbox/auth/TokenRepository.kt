package io.codegeet.sandbox.auth

import org.springframework.data.repository.CrudRepository

interface TokenRepository : CrudRepository<Token, String> {

}
