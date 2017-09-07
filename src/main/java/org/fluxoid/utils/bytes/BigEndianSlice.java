package org.fluxoid.utils.bytes;

public class BigEndianSlice extends AbstractSlice{


    public BigEndianSlice(byte[] data, int offset, int len) {
        super(data, offset, len);
    }

    @Override
    public int unsignedToInt() {
        int res = 0;
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            res += (data[offset - n + len -1] & 0xff) << shift;
        }

        return res;
    }

    @Override
    public void putUnsigned(int val) {
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            data[offset + len -1 - n] = (byte) ((val >>> shift) & 0xff);
        }

    }
}
