package me.alzz.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum Raw implements Command {

    Instance;

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(0);
    }

    public void write(OutputStream out, int val) throws IOException {
        out.write(val);
    }

    public void write(OutputStream out, byte val) throws IOException {
        out.write(val);
    }

    public void write(OutputStream out, String string, String charsetName) throws IOException {
        out.write(string.getBytes(charsetName));
    }

    public void write(OutputStream out, byte[] data) throws IOException {
        out.write(data);
    }

}
