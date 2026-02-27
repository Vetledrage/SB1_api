package com.vetled.store

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(HomeController::class)
class HomeControllerWebTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `root returns index html view`() {
        mockMvc.get("/")
            .andExpect {
                status { isOk() }
                view { name("index.html") }
            }
    }
}

