package org.fluxoid.utils.bytes;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LittleEndianArrayTest {

    @Test
    public void unsignedEncodeDecode()  {
        final int offset = 0;
        final int len = 4;
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianArray slice = new LittleEndianArray(test1);
        assertTrue(slice.unsignedToInt(offset, len) == Integer.MAX_VALUE);


        slice.putUnsigned(offset, len, Integer.MAX_VALUE);
        assertTrue(slice.unsignedToInt(offset, len) == Integer.MAX_VALUE);
        assertTrue(slice.unsignedToLong(offset, len) == Integer.MAX_VALUE);



    }

    @Test
    public void unsignedEncodeDecode2()  {
        final int offset = 1;
        final int len = 2;
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianArray slice = new LittleEndianArray(test1);
        slice.putUnsigned(offset, len, 0xabcd);
        assertEquals(0xabcd, slice.unsignedToInt(offset, len));
    }

    @Test
    public void signedEncodeDecode()  {
        final int offset = 0;
        final int len = 4;
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianArray slice = new LittleEndianArray(test1);

        slice.putSigned(offset, len, Integer.MIN_VALUE);
        assertTrue(slice.signedToInt(offset, len) == Integer.MIN_VALUE);

        slice.putSigned(offset, len, Integer.MAX_VALUE);
        assertTrue(slice.signedToInt(offset, len) == Integer.MAX_VALUE);

    }


}
