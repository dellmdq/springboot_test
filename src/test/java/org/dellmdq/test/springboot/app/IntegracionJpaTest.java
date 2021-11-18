package org.dellmdq.test.springboot.app;

import org.dellmdq.test.springboot.app.models.Cuenta;
import org.dellmdq.test.springboot.app.repositories.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
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

    @Test
    void save(){
        //Given
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        //When
        Cuenta cuentaSaved = cuentaRepository.save(cuentaPepe);//(1)

        //When
        //Cuenta cuentaSaved = cuentaRepository.findByPersona("Pepe").orElseThrow();// ESTO ESTA DEMÁS. no es necesario crearla. La instanciamos con el retorno del repo.(1)

        //Then
        assertEquals("Pepe", cuentaSaved.getPersona());
        assertEquals("3000",cuentaSaved.getSaldo().toPlainString());
//        assertEquals(3,cuentaSaved.getId()); //NO RECOMENDABLE. puede fallar debido a modificaciones previas en la bd y que luego no se haya hecho el ROLLBACK
    }

    @Test
    void update(){
        //Given
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        //When
        Cuenta cuentaSaved = cuentaRepository.save(cuentaPepe);//(1)

        //When
        //Cuenta cuentaSaved = cuentaRepository.findByPersona("Pepe").orElseThrow();// ESTO ESTA DEMÁS. no es necesario crearla. La instanciamos con el retorno del repo.(1)

        //Then
        assertEquals("Pepe", cuentaSaved.getPersona());
        assertEquals("3000",cuentaSaved.getSaldo().toPlainString());
//        assertEquals(3,cuentaSaved.getId()); //NO RECOMENDABLE. puede fallar debido a modificaciones previas en la bd y que luego no se haya hecho el ROLLBACK

        //When
        cuentaSaved.setSaldo(new BigDecimal("3800"));
        Cuenta cuentaUpdated = cuentaRepository.save(cuentaSaved);

        //Then
        assertEquals("Pepe", cuentaUpdated.getPersona());
        assertEquals("3800", cuentaUpdated.getSaldo().toPlainString());
    }

    @Test
    void delete() {
        Cuenta cuenta = cuentaRepository.findById(2L).orElseThrow();
        assertEquals("John", cuenta.getPersona());//verificamos que obtuvimos la persona que buscabamos

        cuentaRepository.delete(cuenta);

        //probamos que la persona se elimino. Al buscarla el repo lanzará una excepción. Validamos esto...
        assertThrows(NoSuchElementException.class, () -> {
            //cuentaRepository.findByPersona("John").orElseThrow();//buscando por nombre
            cuentaRepository.findById(2L).orElseThrow();//buscando por persona
        });

        assertEquals(1, cuentaRepository.findAll().size());//validamos por el tamaño de la lista
    }
}
