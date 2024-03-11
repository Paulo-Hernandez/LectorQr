package com.example.ims;

import java.io.Serializable;

public class RegistroTemporal implements Serializable {
    private String palet;
    private String cajas;
    private String qr;

    public RegistroTemporal(String palet, String cajas, String qr) {
        this.palet = palet;
        this.cajas = cajas;
        this.qr = qr;
    }

    public String getPalet() {
        return palet;
    }

    public String getCajas() {
        return cajas;
    }

    public String getCodigoQR() {
        return qr;
    }
}

