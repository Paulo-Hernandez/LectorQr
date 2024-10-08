package com.example.ims;

public class lecturavalida {
    private String codigo;
    private String palet;
    private String cajas;
    private String embalador;

    // Constructor
    public lecturavalida(String codigo, String palet,String caja, String emablador) {
        this.codigo = codigo;
        this.palet = palet;
        this.cajas = caja;
        this.embalador = emablador;
    }

    // Getters
    public String getCodigo() {
        return codigo;
    }

    public String getPalet() {
        return palet;
    }

    public String getCajas() {
        return cajas;
    }

    public String getEmbalador() {
        return embalador;
    }

    // Setters
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setPalet(String palet) {
        this.palet = palet;
    }

    public void setCajas(String caja) {
        this.cajas = caja;
    }

    public void setEmbalador(String embalador) {
        this.embalador = embalador;
    }

    // Optional: Override toString for easy printing
    @Override
    public String toString() {
        return "Codigo: " + codigo + ", Palet: " + palet;
    }
}
