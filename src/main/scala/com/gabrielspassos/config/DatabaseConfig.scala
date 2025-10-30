package com.gabrielspassos.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@Configuration
@EnableJdbcRepositories(basePackages = Array("com.gabrielspassos.dao.repository"))
class DatabaseConfig
