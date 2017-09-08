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
        public void put(int offset, int len, int val) {

        }

    }

    @Test
    public void testPartialByteNibble() {
        byte [] data = new byte [] {(byte) 0xab};
        AbstractByteArray test = new Generic(data);
        test.putPartialByte(0,0x0F, 7);
        assertEquals(0xa7, 0xff & data[0]);
    }

    @Test
    public void testPartialHighNibble() {
        byte [] data = new byte [] {(byte) 0xab};
        AbstractByteArray test = new Generic(data);
        test.putPartialByte(0,0xF0, 7);
        assertEquals(0x7b, 0xff & data[0]);
    }

    @Test
    public void testBitLevel() {
        byte [] data = new byte [] {(byte) 0b10101010};
        AbstractByteArray test = new Generic(data);
        test.putPartialByte(0,0b100, 1);
        assertEquals(0b10101110, 0xff & data[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badMask() {
        AbstractByteArray test = new Generic(null);
        test.putPartialByte(0,0x1ff, 1);
    }

}
