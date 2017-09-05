package org.fluxoid.utils.bytes;

import org.cowboycoders.ant.utils.BigIntUtils;
import org.fluxoid.utils.Format;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.Normalizer;

public class LittleEndianSlice extends AbstractSlice {

    public static final byte SIGN_BYTE_MASK = (byte) 0b1000_0000;

    public LittleEndianSlice(byte[] data, int offset, int len) {
        super(data, offset, len);
    }

    @Override
    public int unsignedToInt() {
        assert len < 4;
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

    public void putUnsigned(int val) {
        assert val >= 0;
        for (int n = 0; n < len; n++) {
            int shift = 8 * n; // bytes to bits
            int mask = ((0xff << shift) & Integer.MAX_VALUE) >>> shift;
            data[offset + n] = (byte) ((val >>> shift) & mask);
        }

    }

    public static void  main(String[] args) {
        System.out.printf("%x\n", Integer.MAX_VALUE);
        System.out.printf("%x\n", (long)Math.pow(2,32) -1);
        System.out.printf("%x\n", (byte)-127);
        int test = Integer.MAX_VALUE & ((byte) -127);
        System.out.printf("%x\n", test);
        int m = Integer.MAX_VALUE;
        int n = 3;
        int shift = 8 * n;
        int mask = 0xff << shift;
        System.out.printf("%x\n", mask & Integer.MAX_VALUE);
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianSlice slice = new LittleEndianSlice(test1,0,4);
        System.out.println(slice.unsignedToInt() == Integer.MAX_VALUE);
        slice.putUnsigned(Integer.MAX_VALUE);
        System.out.println(slice.unsignedToInt() == Integer.MAX_VALUE);
        System.out.println(slice.unsignedToLong() == Integer.MAX_VALUE);
        slice.putSigned(-1234);
        System.out.printf("%x\n", -1234);
        System.out.println(Format.bytesToString(slice.data));
        System.out.println(slice.signedToInt());


        slice.putSigned(Integer.MIN_VALUE);
        System.out.println(slice.signedToInt() == Integer.MIN_VALUE);
        slice.putSigned(Integer.MAX_VALUE);
        System.out.printf("%x\n", Integer.MAX_VALUE);
        System.out.println(Format.bytesToString(slice.data));
        System.out.println(slice.signedToInt() == Integer.MAX_VALUE);

        BigInteger bi = BigIntUtils.convertUnsignedByte((byte)255);
        System.out.println(Format.bytesToString(bi.toByteArray()));

        BigInteger bi2 = BigIntUtils.convertByte((byte) 255);
        System.out.println(bi2);
        System.out.println(Format.bytesToString(bi2.toByteArray()));
        byte [] data = new byte[2];
        ByteBuffer buff = ByteBuffer.allocate(4);
        buff.putInt(-127);
        ByteBuffer buff3 = ByteBuffer.wrap(data);
        buff3.put(((ByteBuffer)buff.position(2)).slice());
        buff3.order(ByteOrder.LITTLE_ENDIAN);
        System.out.println(Format.bytesToString(data));

    }
}
