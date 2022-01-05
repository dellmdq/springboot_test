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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

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
                .jsonPath("$.mensaje").value( valor -> assertEquals("Transferencia realizada con éxito.", valor))
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
                   assertNotNull(cuenta);
                   assertEquals("John", cuenta.getPersona());
                   assertEquals("2100.00",cuenta.getSaldo().toPlainString());
                });
    }

    @Test
    @Order(4)
    void testListar() {
        webTestClient.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].persona").isEqualTo("Andrés")
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].saldo").isEqualTo(900)
                .jsonPath("$[1].persona").isEqualTo("John")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].saldo").isEqualTo(2100)
                .jsonPath("$").isArray()
                .jsonPath("$").value(hasSize(2));
    }

    @Test
    @Order(5)
    void testListar2() {
        webTestClient.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .consumeWith(response -> {
                    List<Cuenta> cuentas = response.getResponseBody();
                    assertNotNull(cuentas);
                    assertEquals(2, cuentas.size());
                    assertEquals(1L, cuentas.get(0).getId());
                    assertEquals("Andrés", cuentas.get(0).getPersona());
                    assertEquals(900, cuentas.get(0).getSaldo().intValue());//expected como entero
                    assertEquals(2L, cuentas.get(1).getId());
                    assertEquals("John", cuentas.get(1).getPersona());
                    assertEquals("2100.0", cuentas.get(1).getSaldo().toPlainString());//expected como string
                })
                //distintas formas y modulos para testear tamaño de la lista
                .hasSize(2)
                .value(hasSize(2));
    }

    @Test
    @Order(6)
    void testGuardar() {
        //given
        Cuenta cuenta = new Cuenta(null, "Pepe", new BigDecimal("3000"));

        //when
        webTestClient.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta)
                .exchange()
        //then
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.persona").isEqualTo("Pepe")
                .jsonPath("$.persona").value(is("Pepe"))
                .jsonPath("$.saldo").isEqualTo(3000);
    }
    /**Mismo test pero utilizando consumeWith para validar los campos.*/
    @Test
    @Order(7)
    void testGuardar2() {
        //given
        Cuenta cuenta = new Cuenta(null, "Pepa", new BigDecimal("3500"));

        //when
        webTestClient.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Cuenta.class)
                .consumeWith(response -> {
                    Cuenta c = response.getResponseBody();
                    //validamos la respuesta
                    assertNotNull(c);
                    assertEquals(4L, c.getId());
                    assertEquals("Pepa", c.getPersona());
                    assertEquals("3500", c.getSaldo().toPlainString());
                });
    }
}