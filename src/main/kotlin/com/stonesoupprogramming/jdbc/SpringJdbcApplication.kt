package com.stonesoupprogramming.jdbc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
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

//Define a data class that maps to both our
//form and database table
data class User(var firstName: String = "",
                var lastName: String = "",
                var email: String = "",
                var phone: String = "")


@Configuration
class Configuration {

    //First configure a data source that
    //generates an embedded db
    @Bean(name = arrayOf("dataSource"))
    fun dataSource(): DataSource {

        //This will create a new embedded database and run the schema.sql script
        return EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("schema.sql")
                .build()
    }

    //Create a JdbcTemplate Bean that connects to our database
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
        //We can use SimpleJdbcInsert to insert a value into our table
        //The becomes super concise when combined with Kotlins apply and mapOf functions
        SimpleJdbcInsert(jdbcTemplate).withTableName("USERS").apply {
            setGeneratedKeyName("id")
            execute(
                    mapOf("first_name" to user.firstName,
                            "last_name" to user.lastName,
                            "email" to user.email,
                            "phone" to user.phone))
        }
    }

    //This allows us to query the Users table and return a list of users
    //This is one method call to jdbcTemplate with a lambda expression which makes the code
    //incredibly concise
    fun allUsers(): List<User> = jdbcTemplate.query("SELECT FIRST_NAME, LAST_NAME, EMAIL, PHONE FROM USERS",
            { rs: ResultSet, _: Int ->
                User(rs.getString("first_name"), rs.getString("last_name"), rs.getString("email"), rs.getString("phone"))
            })
}

fun main(args: Array<String>) {
    SpringApplication.run(SpringJdbcApplication::class.java, *args)
}
