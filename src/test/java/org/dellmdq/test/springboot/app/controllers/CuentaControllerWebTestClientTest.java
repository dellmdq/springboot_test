package org.dellmdq.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dellmdq.test.springboot.app.models.TransaccionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class CuentaControllerWebTestClientTest {

    @Autowired
    private WebTestClient webTestClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    /**No es necesario usar un mapper para pasar el dto a json usando el object mapper
     * el webTestClient se encarga de hacer ese trabajo.
     * Vamos a testear la respuesta JSON*/
    @Test
    void testTransferir() throws JsonProcessingException {
        //Given
        TransaccionDTO transaccionDTO = new TransaccionDTO();
        transaccionDTO.setCuentaOrigenId(1L);
        transaccionDTO.setCuentaDestinoId(2L);
        transaccionDTO.setBancoId(1L);
        transaccionDTO.setMonto(new BigDecimal("100"));

        Map<String, Object> response = new HashMap<>();//armamos nuestro response
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("mensaje", "Transferencia realizada con éxito.");
        response.put("transaccion", transaccionDTO);

        //When
        webTestClient.post().uri("/api/cuentas/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transaccionDTO)
                //a cambio, a partir de aca viene la respuesta, lo que esperamos testear.
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.mensaje").isNotEmpty()
                .jsonPath("$.mensaje").value(is("Transferencia realizada con éxito."))
                //lo mismo a la linea anterior pero con lambdas
                .jsonPath("$.mensaje").value( valor -> {
                    assertEquals("Transferencia realizada con éxito.", valor);
                })
                .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con éxito.")
                .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(transaccionDTO.getCuentaOrigenId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                //Ahora probamos con Map<String, Object> response. Necesitamos el object mapper.
                //Convertimos map en un string con estructura json!
                .json(objectMapper.writeValueAsString(response));
    }
}