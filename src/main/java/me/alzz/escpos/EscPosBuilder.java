package me.alzz.escpos;

import me.alzz.escpos.command.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;

public class EscPosBuilder {

    /**
     * 58毫米打印机
     */
    public static final int TYPE_58MM = 0;
    /**
     * 80毫米打印机
     */
    public static final int TYPE_80MM = 1;

    private final ByteArrayOutputStream out;
    private final int width;

    /**
     * 默认编码为 utf-8
     */
    private String charsetName = "UTF-8";

    public EscPosBuilder() {
        this(TYPE_58MM);
    }

    public EscPosBuilder(int type) {
        this(type, "UTF-8");
    }

    public EscPosBuilder(String charsetName) {
        this(TYPE_58MM, charsetName);
    }

    public EscPosBuilder(int type, String charsetName) {
        this.out = new ByteArrayOutputStream();
        if (type == TYPE_58MM) {
            width = 32;
        } else {
            width = 47;
        }
        this.charsetName = charsetName;
    }

    public EscPosBuilder initialize() {
        Initialize.Instance.uncheckedWrite(out);
        return this;
    }

    public EscPosBuilder raw(int val) {
        try {
            Raw.Instance.write(out, val);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public EscPosBuilder raw(byte val) {
        try {
            Raw.Instance.write(out, val);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public EscPosBuilder raw(byte... bytes) {
        if (bytes != null)
            try {
                Raw.Instance.write(out, bytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        return this;
    }

    public EscPosBuilder text(String text) {
        if (text != null)
            try {
                Raw.Instance.write(out, text, charsetName);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        return this;
    }

    public EscPosBuilder br() {
        return text("\r\n");
    }

    public EscPosBuilder feed() {
        Feed.Instance.uncheckedWrite(out);
        return this;
    }

    public EscPosBuilder feed(int lines) {
        try {
            FeedLines.Instance.write(out, lines <= 0 ? 1 : lines);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public EscPosBuilder font(Font font) {
        if (font != null) {
            font.uncheckedWrite(out);
        }
        return this;
    }

    public EscPosBuilder font(GSFont font) {
        if (font != null) {
            font.uncheckedWrite(out);
        }
        return this;
    }

    public EscPosBuilder align(Align align) {
        if (align != null) {
            align.uncheckedWrite(out);
        }
        return this;
    }

    public EscPosBuilder cut(Cut cut) {
        if (cut != null) {
            cut.uncheckedWrite(out);
        }
        return this;
    }

    public EscPosBuilder kick(DrawerKick kick) {
        if (kick != null) {
            try {
                kick.write(out);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return this;
    }

    public EscPosBuilder kick(DrawerKick kick, int t1Pulse, int t2Pulse) {
        if (kick != null) {
            try {
                kick.write(out, t1Pulse <= 0 ? 0 : t1Pulse, t2Pulse <= 0 ? 0 : t2Pulse);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return this;
    }

    public EscPosBuilder chineseMode(boolean on) {
        if (on) {
            out.write(28);
            out.write(38);
        } else {
            out.write(28);
            out.write(46);
        }

        return this;
    }

    public EscPosBuilder bold(Bold bold) {
        if (bold != null) {
            bold.uncheckedWrite(out);
        }

        return this;
    }

    /**
     * 分隔线
     */
    public EscPosBuilder line(char divider) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < width; i++) {
            str.append(divider);
        }
        text(str.toString()).br();
        return this;
    }

    /**
     * 一行
     */
    public EscPosBuilder line(String text) {
        return text(text).br();
    }

    /**
     * 居中显示
     */
    public EscPosBuilder center(String text) {
        return align(Align.CENTER).text(text).br();
    }

    /**
     * 居中显示
     */
    public EscPosBuilder center(String text, char divider) {
        try {
            StringBuilder str = new StringBuilder();
            int textWidth = text.getBytes("GBK").length;
            int space = (width - textWidth) / 2;
            for (int i = 0; i < space; i++) {
                str.append(divider);
            }
            str.append(text);
            for (int i = 0; i < (width - space - textWidth); i++) {
                str.append(divider);
            }

            text(str.toString()).br();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 打印至一行中
     */
    public EscPosBuilder line(String text1, float ratio1, String text2, float ratio2, String text3) {

        int cell1Width = (int) (width * ratio1);
        int cell2Width = (int) (width * ratio2);
        int cell3Width = width - cell1Width - cell2Width;
        if (cell3Width <= 0) {
            return this;
        }

        String line;
        try {
            final int text1Width = text1.getBytes("GBK").length;
            final int text2Width = text2.getBytes("GBK").length;
            final int text3Width = text3.getBytes("GBK").length;

            final int start2 = cell1Width + cell2Width - text2Width;
            if (start2 <= 0) {
                return this;
            }

            int end1 = text1Width % width;
            if (end1 < start2) {
                int spaceWidth = start2 - end1;
                line = String.format("%-" + (text1.length() + spaceWidth) + "s%s", text1, text2);
            } else {
                line = String.format("%s\r\n%-" + start2 + "s%s", text1, " ", text2);
            }

            final int end2 = cell1Width + cell2Width;
            final int start3 = width - text3Width;
            if (end2 < start3) {
                line += String.format("%" + (width - end2) + "s", text3);
            } else {
                line += String.format("\r\n%" + width + "s", text3);
            }

            text(line).br();
        } catch (UnsupportedEncodingException e) {
            return this;
        }

        return this;
    }

    /**
     * 打印至一行中
     */
    public EscPosBuilder line(String left, String right) {
        String line;
        try {
            int text1Width = left.getBytes("GBK").length;
            while (text1Width > width) {
                text1Width -= width;
            }
            final int text2Width = right.getBytes("GBK").length;

            final int start2 = width - text2Width;
            if (text1Width < start2) {
                line = String.format("%s%" + (width - text1Width) + "s", left, right);
            } else {
                line = String.format("%s\r\n%" + width + "s", left, right);
            }

            text(line).br();
        } catch (UnsupportedEncodingException e) {
            return this;
        }

        return this;
    }

    public EscPosBuilder left(String text, float ratio) {
        final int cellWidth = (int) (ratio * width);
        text(text);
        try {
            int textWidth = text.getBytes("GBK").length;
            while (textWidth > width) {
                textWidth -= width;
            }

            if (textWidth >= cellWidth) {
                textWidth -= cellWidth;
                int alignWidth = width - textWidth;
                for (int i = 0; i < alignWidth; i++) {
                    text(" ");
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public byte[] getBytes() {
        return out.toByteArray();
    }

    public EscPosBuilder reset() {
        out.reset();
        return this;
    }

    @Override
    public String toString() {
        return out.toString();
    }

}