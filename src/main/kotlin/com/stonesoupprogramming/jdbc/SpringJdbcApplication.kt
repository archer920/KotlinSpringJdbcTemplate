package com.stonesoupprogramming.jdbc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import java.sql.ResultSet
import javax.sql.DataSource

@SpringBootApplication
class SpringJdbcApplication

data class User(var firstName: String = "",
                var lastName: String = "",
                var email: String = "",
                var phone: String = "")

@Configuration
class Configuration {

    @Bean(name = arrayOf("dataSource"))
    fun dataSource(): DataSource {

        //This will create a new embedded database and run the schema.sql script
        return EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("schema.sql")
                .build()
    }

    @Bean
    fun jdbcTemplate(@Qualifier("dataSource") dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }
}

@Controller
@RequestMapping("/")
class IndexController(@Autowired var indexService: IndexService) {

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun doGet(model: Model): String {
        model.addAttribute("user", User())
        model.addAttribute("allUsers", indexService.allUsers())
        return "index"
    }

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun doPost(model: Model, user: User): String {
        indexService.addUser(user)

        model.addAttribute("user", User())
        model.addAttribute("allUsers", indexService.allUsers())
        return "index"
    }
}

@Service
class IndexService(@Autowired var indexRepository: IndexRepository) {

    fun addUser(user: User) {
        indexRepository.addUser(user)
    }

    fun allUsers(): List<User> {
        return indexRepository.allUsers()
    }
}

@Repository
class IndexRepository(@Autowired var jdbcTemplate: JdbcTemplate) {


    fun addUser(user: User) {
        SimpleJdbcInsert(jdbcTemplate).withTableName("USERS").apply {
            setGeneratedKeyName("id")
            execute(
                    mapOf("first_name" to user.firstName,
                            "last_name" to user.lastName,
                            "email" to user.email,
                            "phone" to user.phone))
        }
    }

    fun allUsers(): List<User> = jdbcTemplate.query("SELECT FIRST_NAME, LAST_NAME, EMAIL, PHONE FROM USERS",
            { rs: ResultSet, _: Int ->
                User(rs.getString("first_name"), rs.getString("last_name"), rs.getString("email"), rs.getString("phone"))
            })
}

fun main(args: Array<String>) {
    SpringApplication.run(SpringJdbcApplication::class.java, *args)
}
