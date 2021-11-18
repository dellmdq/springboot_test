package org.dellmdq.test.springboot.app.services;

import org.dellmdq.test.springboot.app.models.Banco;
import org.dellmdq.test.springboot.app.models.Cuenta;
import org.dellmdq.test.springboot.app.repositories.BancoRepository;
import org.dellmdq.test.springboot.app.repositories.CuentaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CuentaServiceImpl implements CuentaService {
    private BancoRepository bancoRepository;
    private CuentaRepository cuentaRepository;

    public CuentaServiceImpl(BancoRepository bancoRepository, CuentaRepository cuentaRepository) {
        this.bancoRepository = bancoRepository;
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public Cuenta findById(Long id) {
        return cuentaRepository.findById(id).orElseThrow();
    }

    @Override
    public int revisarTotalTransferencias(Long bancoId) {
        Banco banco = bancoRepository.findById(bancoId).orElseThrow();
        return banco.getTotalTransferNumber();
    }

    @Override
    public BigDecimal revisarSaldo(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow();
        return cuenta.getSaldo();
    }

    @Override
    public void transferir(Long numCuentaOrigen, Long numCuentaDestino, BigDecimal monto, Long bancoId) {

        Cuenta cuentaOrigen = cuentaRepository.findById(numCuentaOrigen).orElseThrow();
        cuentaOrigen.debito(monto);
        cuentaRepository.save(cuentaOrigen);

        Cuenta cuentaDestino = cuentaRepository.findById(numCuentaDestino).orElseThrow();
        cuentaDestino.credito(monto);
        cuentaRepository.save(cuentaDestino);

        //ponemos las sentencias del banco aquí ya que de haber una excepción no debe modificarse la info del banco. antes estaba al principio del método.
        Banco banco = bancoRepository.findById(bancoId).orElseThrow();//podría ser una variable del usuario, lo dejamos estatico por ser una variable de contexto
        int totalTransferNumber = banco.getTotalTransferNumber();
        banco.setTotalTransferNumber(++totalTransferNumber);
        //aumentamos en uno el total de las transferencias
        bancoRepository.save(banco);
    }
}