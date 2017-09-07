package org.fluxoid.utils.bytes;
import org.fluxoid.utils.bytes.LittleEndianSlice;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LittleEndianSliceTest {

    @Test
    public void unsignedEncodeDecode()  {

        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianSlice slice = new LittleEndianSlice(test1,0,4);
        assertTrue(slice.unsignedToInt() == Integer.MAX_VALUE);


        slice.putUnsigned(Integer.MAX_VALUE);
        assertTrue(slice.unsignedToInt() == Integer.MAX_VALUE);
        assertTrue(slice.unsignedToLong() == Integer.MAX_VALUE);



    }

    @Test
    public void unsignedEncodeDecode2()  {
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianSlice slice = new LittleEndianSlice(test1,1,2);
        slice.putUnsigned(0xabcd);
        assertEquals(0xabcd, slice.unsignedToInt());
    }

    @Test
    public void signedEncodeDecode()  {
        byte [] test1 = new byte[] {(byte)255,(byte)255,(byte)255,(byte)255};
        LittleEndianSlice slice = new LittleEndianSlice(test1,0,4);

        slice.putSigned(Integer.MIN_VALUE);
        assertTrue(slice.signedToInt() == Integer.MIN_VALUE);

        slice.putSigned(Integer.MAX_VALUE);
        assertTrue(slice.signedToInt() == Integer.MAX_VALUE);

    }


}
