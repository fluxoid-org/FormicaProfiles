package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Page 2
 * Created by fluxoid on 02/01/17.
 */
public class CalibrationProgress  implements AntPage {

    public static final int PAGE_NUMBER = 2;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    private static final int OFFSET_IN_PROGRESS_MASK = 0x40;
    private static final int SPINDOWN_IN_PROGRESS_MASK = 0x80;
    private static final int CONDITION_OFFSET = 2;
    private static final int FLAG_OFFSET = 1;
    private static final int TEMP_OFFSET = 3;
    private static final int SPEED_OFFSET = 4;
    private static final int SPINDOWN_OFFSET = 6;

    private final Defines.SpeedCondition speedState;
    private final Defines.TemperatureCondition tempState;
    private final boolean offsetPending;
    private final boolean spinDownPending;

    public Defines.SpeedCondition getSpeedState() {
        return speedState;
    }

    public Defines.TemperatureCondition getTempState() {
        return tempState;
    }

    public boolean isOffsetPending() {
        return offsetPending;
    }

    public boolean isSpinDownPending() {
        return spinDownPending;
    }

    /**
     *  Range: 0ms - 65534ms
     * @return ideal spinDowntime
     */
    public Integer getTargetSpinDownTime() {
        return targetSpinDownTime;
    }

    /**
     *  0m/s - 65.534m/s
     * @return speed that must be reached before letting wheel coast
     */
    public BigDecimal getTargetSpeed() {
        return targetSpeed;
    }

    /**
     * -25C - +100C, 0.5C resolution
     */
    public BigDecimal getTemp() {
        return temp;
    }

    private final Integer targetSpinDownTime;
    private final BigDecimal targetSpeed;
    private final BigDecimal temp;

    public static class CalibrationProgressPayload implements AntPacketEncodable {
        private Defines.SpeedCondition speedState = Defines.SpeedCondition.UNRECOGNIZED;
        private Defines.TemperatureCondition tempState = Defines.TemperatureCondition.UNRECOGNIZED;
        private boolean offsetPending = false;
        private boolean spinDownPending = false;
        private Integer targetSpinDownTime = null;
        private BigDecimal targetSpeed = null;
        private BigDecimal temp = null;

        public Defines.SpeedCondition getSpeedState() {
            return speedState;
        }

        public CalibrationProgressPayload setSpeedState(Defines.SpeedCondition speedState) {
            this.speedState = speedState;
            return this;
        }

        public Defines.TemperatureCondition getTempState() {
            return tempState;
        }

        public CalibrationProgressPayload setTempState(Defines.TemperatureCondition tempState) {
            this.tempState = tempState;
            return this;
        }

        public boolean isOffsetPending() {
            return offsetPending;
        }

        public CalibrationProgressPayload setOffsetPending(boolean offsetPending) {
            this.offsetPending = offsetPending;
            return this;
        }

        public boolean isSpinDownPending() {
            return spinDownPending;
        }

        public CalibrationProgressPayload setSpinDownPending(boolean spinDownPending) {
            this.spinDownPending = spinDownPending;
            return this;
        }

        public Integer getTargetSpinDownTime() {
            return targetSpinDownTime;
        }

        public CalibrationProgressPayload setTargetSpinDownTime(Integer targetSpinDownTime) {
            this.targetSpinDownTime = targetSpinDownTime;
            return this;
        }

        public BigDecimal getTargetSpeed() {
            return targetSpeed;
        }

        public CalibrationProgressPayload setTargetSpeed(BigDecimal targetSpeed) {
            this.targetSpeed = targetSpeed;
            return this;
        }

        public BigDecimal getTemp() {
            return temp;
        }

        public CalibrationProgressPayload setTemp(BigDecimal temp) {
            this.temp = temp;
            return this;
        }

        public void encode(final byte [] packet) {
            packet[0] = PAGE_NUMBER;
            LittleEndianArray littleEndianView = new LittleEndianArray(packet);
            setFlag(offsetPending, packet, FLAG_OFFSET, OFFSET_IN_PROGRESS_MASK);
            setFlag(spinDownPending, packet, FLAG_OFFSET, SPINDOWN_IN_PROGRESS_MASK);
            littleEndianView.putPartialByte(CONDITION_OFFSET, 0b1100_0000, speedState.getIntValue());
            littleEndianView.putPartialByte(CONDITION_OFFSET, 0b0011_0000, tempState.getIntValue());
            if (temp == null) {
                packet[TEMP_OFFSET] = (byte) (0xff & UNSIGNED_INT8_MAX);
            } else {
                BigDecimal n = temp.add(new BigDecimal(25)).multiply(new BigDecimal(2)).setScale(0, RoundingMode.HALF_UP);
                packet[TEMP_OFFSET] = n.byteValue();
            }
            if (targetSpeed == null) {
                littleEndianView.putUnsigned(SPEED_OFFSET, 2, UNSIGNED_INT16_MAX);
            } else {
                BigDecimal n = targetSpeed.multiply(new BigDecimal(1000)).setScale(0, RoundingMode.HALF_UP);
                littleEndianView.putUnsigned(SPEED_OFFSET, 2, n.intValue());
            }
            if (targetSpinDownTime == null) {
                littleEndianView.putUnsigned(SPINDOWN_OFFSET, 2, UNSIGNED_INT16_MAX);
            } else {
                littleEndianView.putUnsigned(SPINDOWN_OFFSET, 2, targetSpinDownTime);
            }
        }


    }



    public CalibrationProgress(final byte[] data) {
        LittleEndianArray littleEndianView = new LittleEndianArray(data);
        offsetPending = booleanFromU8(data[FLAG_OFFSET], OFFSET_IN_PROGRESS_MASK);
        spinDownPending = booleanFromU8(data[FLAG_OFFSET], SPINDOWN_IN_PROGRESS_MASK);

        speedState = Defines.SpeedCondition.getValueFromInt(littleEndianView.getPartialByte(CONDITION_OFFSET, 0b1100_0000));
        tempState = Defines.TemperatureCondition.getValueFromInt(littleEndianView.getPartialByte(CONDITION_OFFSET, 0b0011_0000));
        final int tempRaw = littleEndianView.unsignedToInt(TEMP_OFFSET, 1);
        if (tempRaw != UNSIGNED_INT8_MAX) {
            temp = new BigDecimal(tempRaw).divide(new BigDecimal(2),1, BigDecimal.ROUND_HALF_UP).subtract(new BigDecimal(25));
        } else {
            temp = null;
        }
        final int speedRaw = littleEndianView.unsignedToInt(SPEED_OFFSET, 2);
        if (speedRaw != UNSIGNED_INT16_MAX) {
            targetSpeed = new BigDecimal(speedRaw).divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP);
        } else {
            targetSpeed = null;
        }
        final int spinDownRaw = littleEndianView.unsignedToInt(SPINDOWN_OFFSET, 2);
        if (spinDownRaw != UNSIGNED_INT16_MAX) {
            targetSpinDownTime = spinDownRaw;
        } else {
            targetSpinDownTime = null;
        }



    }


}
