package me.alzz.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum Bold implements Command {

    OFF(0x00),
    ON(0x01);

    private final int code;

    Bold(int code) {
        this.code = code;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(0x1B);
        out.write(0x45);
        out.write(code);
    }
}