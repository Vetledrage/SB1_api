package com.vetleDemo.store;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerViewResolutionTest {

    private final MockMvc mockMvc;

    HomeControllerViewResolutionTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void rootServesStaticIndexHtmlContent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sparebank 1 API call")));
    }
}
