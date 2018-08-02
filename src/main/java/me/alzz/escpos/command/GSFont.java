package me.alzz.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum GSFont implements Command {

    NORMAL(0x00),
    DH(0x01),
    DW(0x10),
    DWDH(0x11);

    private final int n;

    GSFont(int n) {
        this.n = n;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(0x1D);
        out.write(0x21);
        out.write(n);
    }

}
