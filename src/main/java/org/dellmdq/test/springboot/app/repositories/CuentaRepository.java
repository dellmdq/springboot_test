package org.dellmdq.test.springboot.app.repositories;

import org.dellmdq.test.springboot.app.models.Cuenta;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;

/**No es necesario utilizar anotaciones, con heredar de JpaRepository ya esta.*/
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    //?wildcard el 1 señala la cantidad de parametros
    @Query("select c from Cuenta c where c.persona=?1")
    Optional<Cuenta> findByPersona(String persona);

    //no es necesario implementar nada, JpaRepository se encarga
//    List<Cuenta> findAll();
//    Cuenta findById(Long id);
//    void update(Cuenta cuenta);
}
