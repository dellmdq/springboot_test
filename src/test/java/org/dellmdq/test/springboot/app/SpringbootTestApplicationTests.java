package org.dellmdq.test.springboot.app;

import org.dellmdq.test.springboot.app.exceptions.DineroInsuficienteException;
import org.dellmdq.test.springboot.app.models.Banco;
import org.dellmdq.test.springboot.app.models.Cuenta;
import org.dellmdq.test.springboot.app.repositories.BancoRepository;
import org.dellmdq.test.springboot.app.repositories.CuentaRepository;
import org.dellmdq.test.springboot.app.services.CuentaService;

import org.dellmdq.test.springboot.app.services.CuentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.dellmdq.test.springboot.app.Datos.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;


@SpringBootTest
class SpringbootTestApplicationTests {

	@MockBean
	CuentaRepository cuentaRepository;
	@MockBean
	BancoRepository bancoRepository;
	@Autowired//inyección de dependencias cuenta y banco repositories.
	CuentaService cuentaService;

	@BeforeEach
	void setUp(){
//		cuentaRepository = mock(CuentaRepository.class);
//		bancoRepository = mock(BancoRepository.class);
//		cuentaService = new CuentaServiceImpl(bancoRepository, cuentaRepository);

		//seteamos antes de cada método para evitar acoplamiento
//		Datos.CUENTA_001.setSaldo(new BigDecimal("1000"));
//		Datos.CUENTA_002.setSaldo(new BigDecimal("2000"));
//		Datos.BANCO.setTotalTransferNumber(0);
	}

	/**Ojo que los datos son estáticos, deben ser independientes. Un método puede
	 * modificar los datos y afectar el resultado del método siguiente.*/
	@Test
	void contextLoads() {
//		when(cuentaRepository.findById(1L)).thenReturn(Datos.CUENTA_001);
//		when(cuentaRepository.findById(2L)).thenReturn(Datos.CUENTA_002);
//		when(bancoRepository.findById(1L)).thenReturn(Datos.BANCO);

		//reemplazamos por los métodos estáticos para tener variables limpias en cada test
		//modificamos los métodos para que
		when(cuentaRepository.findById(1L)).thenReturn(crearCuenta001());
		when(cuentaRepository.findById(2L)).thenReturn(crearCuenta002());
		when(bancoRepository.findById(1L)).thenReturn(crearBanco());


		BigDecimal saldoOrigen = cuentaService.revisarSaldo(1L);
		BigDecimal saldoDestino = cuentaService.revisarSaldo(2L);

		//chequeamos saldos originales
		assertEquals("1000", saldoOrigen.toPlainString());
		assertEquals("2000", saldoDestino.toPlainString());

		//chequeamos transferencias al comienzo
		cuentaService.transferir(1L, 2L, new BigDecimal("100"), 1L);//(1)

		saldoOrigen = cuentaService.revisarSaldo(1L);
		saldoDestino = cuentaService.revisarSaldo(2L);

		assertEquals("900",saldoOrigen.toPlainString());
		assertEquals("2100",saldoDestino.toPlainString());


		//POR DEFAULT EL TIMES DEL VERIFY ES (1)
		int total = cuentaService.revisarTotalTransferencias(1L);//(2)
		assertEquals(1, total);

		verify(cuentaRepository, times(3)).findById(1L);
		verify(cuentaRepository, times(3)).findById(2L);
		//cambiamos update por save, en JpaRepository el método es save
		verify(cuentaRepository, times(2)).save(any(Cuenta.class));

		verify(bancoRepository, times(2)).findById(1L);//dos veces una en la transferencia (1) y otra en revisarTotalTransferencias (2)
		verify(bancoRepository).save(any(Banco.class));

		verify(cuentaRepository, times(6)).findById(anyLong());
		verify(cuentaRepository, never()).findAll();

	}

	@Test
	void contextLoads2() {
		when(cuentaRepository.findById(1L)).thenReturn(crearCuenta001());
		when(cuentaRepository.findById(2L)).thenReturn(crearCuenta002());
		when(bancoRepository.findById(1L)).thenReturn(crearBanco());

		BigDecimal saldoOrigen = cuentaService.revisarSaldo(1L);
		BigDecimal saldoDestino = cuentaService.revisarSaldo(2L);

		//chequeamos saldos originales
		assertEquals("1000", saldoOrigen.toPlainString());
		assertEquals("2000", saldoDestino.toPlainString());

		/**lanza la excepción al llamar a cuentaOrigen.debito. */
		assertThrows(DineroInsuficienteException.class, () -> {
			cuentaService.transferir(1L, 2L, new BigDecimal("1200"), 1L);
		});

		saldoOrigen = cuentaService.revisarSaldo(1L);
		saldoDestino = cuentaService.revisarSaldo(2L);

		assertEquals("1000",saldoOrigen.toPlainString());//se lanza la excepcion no deberia modificarse
		assertEquals("2000",saldoDestino.toPlainString());//se lanza la excepcion no deberia modificarse

		//POR DEFAULT EL TIMES DEL VERIFY ES (1)
		int total = cuentaService.revisarTotalTransferencias(1L);
		assertEquals(0, total);//lanza la excepcion antes, no se hace la transferencia y queda en cero.

		verify(cuentaRepository, times(3)).findById(1L);//123
		verify(cuentaRepository, times(2)).findById(2L);//12
		verify(cuentaRepository, never()).save(any(Cuenta.class));//nunca se ejecuta el update, se lanza antes la excepcion de dinero insuficiente.

		verify(bancoRepository).findById(1L);//el de transferir no llega a ejecutarse porque se lanza la excepción antes
		verify(bancoRepository, never()).save(any(Banco.class));//no se ejecuta porque se lanza la excepción por dinero insuf

		verify(cuentaRepository, times(5)).findById(anyLong());
		verify(cuentaRepository, never()).findAll();

	}

	@Test
	void contextLoads3() {
		when(cuentaRepository.findById(1L)).thenReturn(crearCuenta001());

		Cuenta cuenta1 = cuentaService.findById(1L);
		Cuenta cuenta2 = cuentaService.findById(1L);

		assertSame(cuenta1, cuenta2);
		assertTrue(cuenta1 == cuenta2);

		assertEquals("Andrés", cuenta1.getPersona());
		assertEquals("Andrés", cuenta2.getPersona());

		verify(cuentaRepository, times(2)).findById(1L);
	}
}
