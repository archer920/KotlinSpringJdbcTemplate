package com.stonesoupprogramming.jdbc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@SpringBootApplication
class SpringJdbcApplication

data class User(var firstName: String = "",
                var lastName: String = "",
                var email : String = "",
                var phone : String = "")

@Controller
@RequestMapping("/")
class IndexController(@Autowired var indexService: IndexService) {

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun doGet(model : Model) : String {
        model.addAttribute("user", User())
        model.addAttribute("allUsers", indexService.allUsers())
        return "index"
    }

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun doPost(model: Model, user : User) : String {
        indexService.addUser(user)

        model.addAttribute("user", User())
        model.addAttribute("allUsers", indexService.allUsers())
        return "index"
    }
}

@Service
class IndexService(@Autowired var indexRepository: IndexRepository) {

    fun addUser(user : User){
        indexRepository.addUser(user)
    }

    fun allUsers() : List<User> {
        return indexRepository.allUsers()
    }
}

@Repository
class IndexRepository(var users: MutableList<User> = ArrayList<User>()) {

    fun addUser(user : User){
        users.add(user)
    }

    fun allUsers() : List<User> {
        return users.toList()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(SpringJdbcApplication::class.java, *args)
}
