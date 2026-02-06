package com.flightspredictor.flights.infra.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = CorrelationIdFilterTest.TestController.class)
@Import(CorrelationIdFilter.class)
class CorrelationIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateCorrelationIdWhenMissing() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/ping").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn();

        String header = result.getResponse().getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(header).isNotBlank();
    }

    @Test
    void shouldEchoCorrelationIdWhenProvided() throws Exception {
        String expected = "test-correlation-id";
        MvcResult result = mockMvc.perform(get("/test/ping")
                        .header(CorrelationIdFilter.CORRELATION_ID_HEADER, expected)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn();

        String header = result.getResponse().getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(header).isEqualTo(expected);
    }

    @RestController
    static class TestController {

        @GetMapping("/test/ping")
        String ping() {
            return "ok";
        }
    }
}
