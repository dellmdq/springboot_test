package org.dellmdq.test.springboot.app.controllers;

import org.dellmdq.test.springboot.app.models.TransaccionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class CuentaControllerWebTestClientTest {

    @Autowired
    private WebTestClient webTestClient;

    /**No es necesario usar un mapper para pasar el dto a json usando el object mapper
     * el webTestClient se encarga de hacer ese trabajo.*/
    @Test
    void testTransferir() {
        //Given
        TransaccionDTO transaccionDTO = new TransaccionDTO();
        transaccionDTO.setCuentaOrigenId(1L);
        transaccionDTO.setCuentaDestinoId(2L);
        transaccionDTO.setBancoId(1L);
        transaccionDTO.setMonto(new BigDecimal("100"));

        //When
        webTestClient.post().uri("http://localhost:8080/api/cuentas/transferir")
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
                .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con éxito")
                .jsonPath("$transacción.cuentaOrigenId").isEqualTo(transaccionDTO.getCuentaOrigenId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString());
    }
}