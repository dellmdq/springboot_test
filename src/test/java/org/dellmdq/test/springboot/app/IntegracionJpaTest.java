package org.dellmdq.test.springboot.app;

import org.dellmdq.test.springboot.app.models.Cuenta;
import org.dellmdq.test.springboot.app.repositories.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class IntegracionJpaTest {

    @Autowired
    CuentaRepository cuentaRepository;

    @Test
    void findById() {
       Optional<Cuenta> cuenta = cuentaRepository.findById(1L);

       assertTrue(cuenta.isPresent());
       assertEquals("Andrés",cuenta.orElseThrow().getPersona());
    }

    @Test
    void findByPerson() {
        Optional<Cuenta> cuenta = cuentaRepository.findByPersona("Andrés");

        assertTrue(cuenta.isPresent());
        assertEquals("Andrés",cuenta.orElseThrow().getPersona());
        assertEquals("1000.00",cuenta.orElseThrow().getSaldo().toPlainString());
    }

    @Test
    void findByPersonThrowException() {
        Optional<Cuenta> cuenta = cuentaRepository.findByPersona("Error");

        //si cuenta no esta presente lanza la excepción.
//        assertThrows(NoSuchElementException.class, () -> {
//            cuenta.orElseThrow();
//        });

        //otra forma de escribir la expresión lambda. abreviando con un método de referencia
        assertThrows(NoSuchElementException.class, cuenta::orElseThrow);

        assertFalse(cuenta.isPresent());
    }

    @Test
    void findAll() {
        List<Cuenta> cuentaList = cuentaRepository.findAll();
        assertFalse(cuentaList.isEmpty());
        assertEquals(2,cuentaList.size());
    }
}
