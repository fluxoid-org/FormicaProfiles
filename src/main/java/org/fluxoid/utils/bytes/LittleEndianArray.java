package org.fluxoid.utils.bytes;

import org.cowboycoders.ant.utils.IntUtils;

public class LittleEndianArray extends AbstractByteArray {

    public static final byte SIGN_BYTE_MASK = (byte) 0b1000_0000;

    public LittleEndianArray(byte[] data) {
        super(data);
    }

    @Override
    public int unsignedToInt(int offset, int len) {
        assert len <= 4;
        int res = 0;
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            int mask = ((0xff << shift) & Integer.MAX_VALUE) >>> shift;
            res += (data[offset + n] & mask) << shift;
        }

        return res;
    }

    public long unsignedToLong(int offset, int len) {
        assert len <= 8;
        int res = 0;
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            long mask = ((0xffL << shift) & Long.MAX_VALUE) >>> shift;
            res += (data[offset + n] & mask) << shift;
        }

        return res;
    }

    public void putSigned(int offset, int len, int val) {
        putUnsigned(offset, len, val);
        if (val < 0) {
            data[offset + len -1] |= SIGN_BYTE_MASK;
        }
    }

    public int signedToInt(int offset, int len) {
        boolean isNegative = (byte) (data[offset + len -1] & SIGN_BYTE_MASK) == SIGN_BYTE_MASK;
        int ret = unsignedToInt(offset, len);
        if (isNegative) ret = (Integer.MIN_VALUE + ret);
        return ret;
    }

    @Override
    public void putUnsigned(int offset, int len, int val) {
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            data[offset + n] = (byte) ((val >>> shift) & 0xff);
        }

    }

}
