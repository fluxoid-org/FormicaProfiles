package org.fluxoid.utils.bytes;

public abstract class AbstractSlice {
    protected final byte[] data;
    protected final int offset;
    protected final int len;

    public AbstractSlice(byte[] data, int offset, int len) {
        this.data = data;
        this.offset = offset;
        this.len = len;
    }

    public byte[] getData() {
        return data;
    }

    public int getOffset() {
        return offset;
    }

    public int getLen() {
        return len;
    }

    public abstract int unsignedToInt();
}
