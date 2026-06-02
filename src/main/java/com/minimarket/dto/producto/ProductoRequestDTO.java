package com.minimarket.dto.producto;

import com.minimarket.dto.common.IdRefDTO;

public class ProductoRequestDTO {

    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private IdRefDTO categoria;

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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public IdRefDTO getCategoria() {
        return categoria;
    }

    public void setCategoria(IdRefDTO categoria) {
        this.categoria = categoria;
    }
}
