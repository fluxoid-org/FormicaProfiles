package org.fluxoid.utils.bytes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractArrayTest {

    private static class Generic extends AbstractByteArray{

        public Generic(byte[] data) {
            super(data);
        }

        @Override
        public int unsignedToInt(int offset, int len) {
            return 0;
        }

        @Override
        public void putUnsigned(int offset, int len, int val) {

        }

    }

    @Test
    public void testPartialByteNibble() {
        byte [] data = new byte [] {(byte) 0xab};
        AbstractByteArray test = new Generic(data);
        test.putPartialByte(0,0x0F, 0xff);
        assertEquals(0xa7, 0xff & data[0]);
    }
}
