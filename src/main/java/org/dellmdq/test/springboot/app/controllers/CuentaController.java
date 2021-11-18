package org.dellmdq.test.springboot.app.controllers;

import org.dellmdq.test.springboot.app.models.Cuenta;
import org.dellmdq.test.springboot.app.models.TransaccionDTO;
import org.dellmdq.test.springboot.app.services.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping("/api/cuentas")
public class CuentaController {

    @Autowired
    CuentaService cuentaService;

    @GetMapping("/{id}")
    @ResponseStatus(OK)
    public Cuenta detalle(@PathVariable(name="id") Long id){//no es necesario poner el name. por default asignara al mismo nombre de parametro
        return cuentaService.findById(id);
    }

    @PostMapping("/transferencia")
    public ResponseEntity<?> transferir(@RequestBody TransaccionDTO transaccionDTO){
        cuentaService.transferir(transaccionDTO.getCuentaOrigenId(),transaccionDTO.getCuentaDestinoId(),
                transaccionDTO.getMonto(),
                transaccionDTO.getBancoId());

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("mensaje", "Transferencia realizada con éxito");
        response.put("transaccion", transaccionDTO);

        return ResponseEntity.ok(transaccionDTO);//aqui devolvemos el JSON
    }


}
