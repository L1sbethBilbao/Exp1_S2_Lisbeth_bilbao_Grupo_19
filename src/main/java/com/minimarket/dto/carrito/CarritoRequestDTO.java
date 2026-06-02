package com.minimarket.dto.carrito;

import com.minimarket.dto.common.IdRefDTO;

public class CarritoRequestDTO {

    private Long id;
    private IdRefDTO usuario;
    private IdRefDTO producto;
    private Integer cantidad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IdRefDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(IdRefDTO usuario) {
        this.usuario = usuario;
    }

    public IdRefDTO getProducto() {
        return producto;
    }

    public void setProducto(IdRefDTO producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
