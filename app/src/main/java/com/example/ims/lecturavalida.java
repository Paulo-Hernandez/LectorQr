package com.example.ims;

public class lecturavalida {
    private String codigo;
    private String palet;
    private String cajas;

    // Constructor
    public lecturavalida(String codigo, String palet,String caja) {
        this.codigo = codigo;
        this.palet = palet;
        this.cajas = caja;
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

    // Optional: Override toString for easy printing
    @Override
    public String toString() {
        return "Codigo: " + codigo + ", Palet: " + palet;
    }
}
