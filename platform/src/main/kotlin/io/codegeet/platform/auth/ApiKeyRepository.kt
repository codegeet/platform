package io.codegeet.platform.auth

import org.springframework.data.repository.CrudRepository

interface ApiKeyRepository : CrudRepository<ApiKey, String>
