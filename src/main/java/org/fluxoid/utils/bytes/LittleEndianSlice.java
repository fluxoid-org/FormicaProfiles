package org.fluxoid.utils.bytes;

import org.cowboycoders.ant.utils.BigIntUtils;
import org.fluxoid.utils.Format;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LittleEndianSlice extends AbstractSlice {

    public static final byte SIGN_BYTE_MASK = (byte) 0b1000_0000;

    public LittleEndianSlice(byte[] data, int offset, int len) {
        super(data, offset, len);
    }

    @Override
    public int unsignedToInt() {
        assert len <= 4;
        int res = 0;
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            int mask = ((0xff << shift) & Integer.MAX_VALUE) >>> shift;
            res += (data[offset + n] & mask) << shift;
        }

        return res;
    }

    public long unsignedToLong() {
        assert len < 8;
        int res = 0;
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            long mask = ((0xffL << shift) & Long.MAX_VALUE) >>> shift;
            res += (data[offset + n] & mask) << shift;
        }

        return res;
    }

    public void putSigned(int val) {
        putUnsigned(val);
        if (val < 0) {
            data[offset + len -1] |= SIGN_BYTE_MASK;
        }
    }

    public int signedToInt() {
        boolean isNegative = (byte) (data[offset + len -1] & SIGN_BYTE_MASK) == SIGN_BYTE_MASK;
        int ret = unsignedToInt();
        if (isNegative) ret = (Integer.MIN_VALUE + ret);
        return ret;
    }

    @Override
    public void putUnsigned(int val) {
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            data[offset + n] = (byte) ((val >>> shift) & 0xff);
        }

    }

}
