package org.dellmdq.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dellmdq.test.springboot.app.models.Cuenta;
import org.dellmdq.test.springboot.app.models.TransaccionDTO;
import org.dellmdq.test.springboot.app.services.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dellmdq.test.springboot.app.Datos.crearCuenta001;
import static org.dellmdq.test.springboot.app.Datos.crearCuenta002;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    /**No hay servidor. No hay request, ni response real. Todas las operaciones
     * son simuladas.*/
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CuentaService cuentaService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void detalle() throws Exception {
        //Given CONTEXTO DE LA PRUEBA
        when(cuentaService.findById(1L)).thenReturn(crearCuenta001().orElseThrow());

        //When. SE HACE LA LLAMADA A NUESTRO CONTROLADOR REAL. EL SERVICE Y LO DEMÁS ES SIMULADO.
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON))
        //Then
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.persona").value("Andrés"))//$ hace ref a la raiz del json.
                .andExpect(jsonPath("$.saldo").value("1000"));

        verify(cuentaService).findById(1L);
    }

    @Test
    void transferir() throws Exception, JsonProcessingException {

        //Given
        TransaccionDTO transaccionDTO = new TransaccionDTO();
        transaccionDTO.setCuentaOrigenId(1L);
        transaccionDTO.setCuentaDestinoId(2L);
        transaccionDTO.setMonto(new BigDecimal("100"));
        transaccionDTO.setBancoId(1L);

        System.out.println(objectMapper.writeValueAsString(transaccionDTO));

        Map<String, Object> response = new HashMap<>();//armamos nuestro response
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("mensaje", "Transferencia realizada con éxito.");
        response.put("transaccion", transaccionDTO);

        System.out.println(objectMapper.writeValueAsString(response));

        //When
        //Recordemos esto es un POST
        mockMvc.perform(post("/api/cuentas/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaccionDTO)))
        //Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.mensaje").value("Transferencia realizada con éxito."))
                .andExpect(jsonPath("$.transaccion.cuentaOrigenId").value(transaccionDTO.getCuentaOrigenId()))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

    }

    @Test
    void findAll() throws Exception {
        //Given
        List<Cuenta> cuentas = Arrays.asList(crearCuenta001().orElseThrow(),crearCuenta002().orElseThrow());
        //El service es un mock, lo que queremos testear es el controller en este contexto
        when(cuentaService.findAll()).thenReturn(cuentas);

        //When
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cuentas").contentType(MediaType.APPLICATION_JSON))
        //Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].persona").value("Andrés"))
                .andExpect(jsonPath("$[1].persona").value("Jorge"))
                .andExpect(jsonPath("$[0].saldo").value("1000"))
                .andExpect(jsonPath("$[1].saldo").value("2000"))
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(content().json(objectMapper.writeValueAsString(cuentas)));

        verify(cuentaService).findAll();
    }

    @Test
    void save() throws Exception {
        //Given
        Cuenta cuenta = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        when(cuentaService.save(any())).then(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        //When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuenta)))
        //Then
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.persona", is("Pepe")))
                .andExpect(jsonPath("$.saldo", is(3000)));

        verify(cuentaService).save(any());
    }
}