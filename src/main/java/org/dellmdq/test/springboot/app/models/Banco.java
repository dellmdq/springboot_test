package org.dellmdq.test.springboot.app.models;

import javax.persistence.*;

@Entity
@Table(name = "bancos")
public class Banco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @Column(name = "total_transferencias")
    private int totalTransferNumber;

    public Banco() {
    }

    public Banco(Long id, String nombre, int totalTransferNumber) {
        this.id = id;
        this.nombre = nombre;
        this.totalTransferNumber = totalTransferNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTotalTransferNumber() {
        return totalTransferNumber;
    }

    public void setTotalTransferNumber(int totalTransferNumber) {
        this.totalTransferNumber = totalTransferNumber;
    }
}
