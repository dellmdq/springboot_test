package org.dellmdq.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dellmdq.test.springboot.app.models.Cuenta;
import org.dellmdq.test.springboot.app.models.TransaccionDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.print.attribute.standard.Media;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.hamcrest.Matchers.*;

/**En este test, vamos a estar usando clientes http y nada es mockeado, se utilizan todos los
 * layers para hacer este test. Recordar que en esta clase, los test modifican los datos
 * a los que tienen acceso los test. Por lo que es buenos utilizar @TestMethodOrder*/
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Order(1)
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
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()//se puede usar con un String.class y modificar abajo json. distintas formas de obtener el body.
                .consumeWith(answer -> {
                    try {
                    JsonNode json = objectMapper.readTree(answer.getResponseBody());
                    assertEquals("Transferencia realizada con éxito.", json.path("mensaje").asText());
                    assertEquals(1L, json.path("transaccion").path("cuentaOrigenId").asLong());
                    assertEquals(LocalDate.now().toString(), json.path("date").asText());
                    assertEquals("100", json.path("transaccion").path("monto").asText());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
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

    //testeamos body usando json path
    @Test
    @Order(2)
    void testDetalle() throws JsonProcessingException {
        //Creamos la cuenta que esperamos obtener. Convertiremos esta cuenta a json.
        Cuenta cuenta = new Cuenta(1L, "Andrés", new BigDecimal("900"));


        webTestClient.get().uri("/api/cuentas/1").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.persona").isEqualTo("Andrés")
                .jsonPath("$.saldo").isEqualTo(900)
                .json(objectMapper.writeValueAsString(cuenta));//validamos json completo
    }

    /*vamos a hacer el mismo test de forma distinta. Convirtiendo el json a una clase Cuenta.class
    * testeamos body usando consumeWith*/
    @Test
    @Order(3)
    void testDetalle2(){
        webTestClient.get().uri("/api/cuentas/2").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Cuenta.class)
                .consumeWith(response ->{
                   Cuenta cuenta = response.getResponseBody();
                   assertEquals("John", cuenta.getPersona());
                   assertEquals("2100.00",cuenta.getSaldo().toPlainString());
                });
    }
}