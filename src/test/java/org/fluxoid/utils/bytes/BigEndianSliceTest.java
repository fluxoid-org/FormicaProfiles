package org.fluxoid.utils.bytes;

import org.fluxoid.utils.Format;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BigEndianSliceTest {

    @Test
    public void oneByte() {
        byte [] data = new byte[] {(byte)0xff};
        BigEndianSlice test = new BigEndianSlice(data,0,1);
        assertEquals(0xff, test.unsignedToInt());

        test.putUnsigned(0xab);
        assertEquals(0xab, test.unsignedToInt());

    }

    @Test
    public void twoBytes() {
        byte [] data = new byte[] {(byte)0xff, (byte) 0xff};
        BigEndianSlice test = new BigEndianSlice(data,0,2);
        assertEquals(0xffff, test.unsignedToInt());

        test.putUnsigned(0xabcd);
        assertEquals(0xabcd, test.unsignedToInt());

    }
}
