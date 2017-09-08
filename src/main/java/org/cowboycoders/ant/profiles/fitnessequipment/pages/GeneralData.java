package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.common.decode.interfaces.DistanceDecodable;
import org.cowboycoders.ant.profiles.common.decode.interfaces.TimeDecodable;
import org.cowboycoders.ant.profiles.common.utils.CounterUtils;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.MathCompat;
import org.fluxoid.utils.RollOverVal;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * p16
 * Created by fluxoid on 02/01/17.
 */
public class GeneralData extends CommonPageData implements AntPage, TimeDecodable, DistanceDecodable {

    public static final int PAGE_NUMBER = 16;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    // these are counters that overflow
    private static final int TIME_OFFSET = 2;
    private static final int DISTANCE_OFFSET = 3;

    private static final int META_OFFSET = 7;
    private static final int SPEED_OFFSET = 4;
    private static final int HR_OFFSET = 6; // heart rate
    private static final int HR_SOURCE_MASK = 0x3;
    private static final int DISTANCE_MASK = 0x4;
    private static final int VIRTUAL_SPEED_MASK = 0x8;

    private static final int TYPE_MASK = 0x1F;
    private static final int TYPE_OFFSET = 1;

    public boolean isDistanceAvailable() {
        return distanceAvailable;
    }

    public int getTimeElapsed() {
        return timeElapsed;
    }

    public Integer getDistanceCovered() {
        return distanceCovered;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public Defines.HeartRateDataSource getHeartRateSource() {
        return heartRateSource;
    }

    public boolean isUsingVirtualSpeed() {
        return usingVirtualSpeed;
    }


    private final boolean distanceAvailable;
    private final int timeElapsed;
    private final Integer distanceCovered;
    private final BigDecimal speed;
    private final Integer heartRate;
    private final Defines.HeartRateDataSource heartRateSource;
    private final boolean usingVirtualSpeed;

    private final Defines.EquipmentType type;

    public Defines.EquipmentType getType() {
        return type;
    }

    @Override
    public int getTicks() {
        return timeElapsed;
    }

    @Override
    public long getTicksDelta(TimeDecodable old) {
        return CounterUtils.calcDelta(UNSIGNED_INT8_MAX, old.getTicks(), getTicks());
    }

    @Override
    public BigDecimal ticksToSeconds(long delta) {
        return new BigDecimal(delta). divide(new BigDecimal(4), 2, RoundingMode.HALF_UP);
    }

    public static class GeneralDataPayload extends CommonPagePayload implements AntPacketEncodable {


        private int timeElapsed = 0;
        private Long distanceCovered;
        private RollOverVal distance = new RollOverVal(UNSIGNED_INT8_MAX);
        private BigDecimal speed;
        private Integer heartRate;
        private Defines.HeartRateDataSource heartRateSource;
        private boolean usingVirtualSpeed;
        private Defines.EquipmentType type;


        public int getTimeElapsed() {
            return timeElapsed;
        }

        public GeneralDataPayload setTimeElapsed(BigDecimal seconds) {
            int temp = seconds.multiply(new BigDecimal(4)).setScale(0, RoundingMode.HALF_UP).intValue();
            RollOverVal val = new RollOverVal(UNSIGNED_INT8_MAX);
            val.setValue(temp);
            timeElapsed = MathCompat.toIntExact(val.get());
            return this;
        }

        /**
         * @param timeElapsed in seconds x 4
         */
        public GeneralDataPayload setTimeElapsed(int timeElapsed) {
            this.timeElapsed = timeElapsed;
            return this;
        }

        public Long getDistanceCovered() {
            return distanceCovered;
        }

        /**
         * @param distanceCovered if larger than 255, will be rolled over to fit in a byte when encoded
         *                        e.g 256 will be encoded as 0, and 257 will be encoded as 1
         */
        public GeneralDataPayload setDistanceCovered(Long distanceCovered) {
            distance.setValue(distanceCovered);
            this.distanceCovered = distanceCovered;
            return this;
        }

        /**
         * @return instantaneous speed
         */
        public BigDecimal getSpeed() {
            return speed;
        }

        public GeneralDataPayload setSpeed(BigDecimal speed) {
            this.speed = speed;
            return this;
        }

        public Integer getHeartRate() {
            return heartRate;
        }

        public GeneralDataPayload setHeartRate(Integer heartRate) {
            this.heartRate = heartRate;
            return this;
        }

        public Defines.HeartRateDataSource getHeartRateSource() {
            return heartRateSource;
        }

        public GeneralDataPayload setHeartRateSource(Defines.HeartRateDataSource heartRateSource) {
            this.heartRateSource = heartRateSource;
            return this;
        }

        public boolean isUsingVirtualSpeed() {
            return usingVirtualSpeed;
        }

        public GeneralDataPayload setUsingVirtualSpeed(boolean usingVirtualSpeed) {
            this.usingVirtualSpeed = usingVirtualSpeed;
            return this;
        }

        public Defines.EquipmentType getType() {
            return type;
        }

        public GeneralDataPayload setType(Defines.EquipmentType type) {
            this.type = type;
            return this;
        }

        @Override
        public GeneralDataPayload setLapFlag(boolean lapflag) {
            return (GeneralDataPayload) super.setLapFlag(lapflag);
        }

        @Override
        public GeneralDataPayload setState(Defines.EquipmentState state) {
            return (GeneralDataPayload) super.setState(state);
        }

        public void encode(final byte[] packet) {
            super.encode(packet);
            LittleEndianArray viewer = new LittleEndianArray(packet);
            final boolean distanceAvailable = distanceCovered != null;
            viewer.putUnsigned(PAGE_OFFSET, 1, PAGE_NUMBER);
            packet[TYPE_OFFSET] = (byte) (0xff & (type.getIntValue() & TYPE_MASK));
            if (distanceAvailable) {
                packet[META_OFFSET] |= DISTANCE_MASK;
            } else {
                packet[META_OFFSET] = clearMaskedBits(packet[META_OFFSET], DISTANCE_MASK);
            }
            viewer.putUnsigned(TIME_OFFSET, 1, timeElapsed);
            if (distanceAvailable) {
                viewer.putUnsigned(DISTANCE_OFFSET, 1, MathCompat.toIntExact(distance.get()));
            }
            if (speed == null) {
                viewer.putUnsigned(SPEED_OFFSET,2, UNSIGNED_INT16_MAX);
            } else {
                BigDecimal conv = speed.multiply(new BigDecimal(1000)).setScale(0, RoundingMode.HALF_UP);
                viewer.putUnsigned(SPEED_OFFSET,2, conv.intValue());
            }
            if (heartRate == null) {
                viewer.putUnsigned(HR_OFFSET, 1, UNSIGNED_INT8_MAX);
            } else {
                viewer.putUnsigned(HR_OFFSET, 1, (int) heartRate);
            }
            packet[META_OFFSET] |= heartRateSource.getIntValue();

            if (usingVirtualSpeed) {
                packet[META_OFFSET] |= VIRTUAL_SPEED_MASK;
            } else {
                packet[META_OFFSET] = clearMaskedBits(packet[META_OFFSET], VIRTUAL_SPEED_MASK);
            }
        }
    }



    public GeneralData(byte [] packet) {
        super(packet);
        LittleEndianArray viewer = new LittleEndianArray(packet);
        distanceAvailable = booleanFromU8(packet[META_OFFSET], DISTANCE_MASK);

        this.timeElapsed = viewer.unsignedToInt(TIME_OFFSET, 1);

        if (distanceAvailable) {
            distanceCovered = viewer.unsignedToInt(DISTANCE_OFFSET, 1);
        } else {
            // IMO null will lead to fewer hard to detect bugs
            distanceCovered = null;
        }

        final int speedRaw = viewer.unsignedToInt(SPEED_OFFSET, 2);
        if (speedRaw != UNSIGNED_INT16_MAX) {
            speed = new BigDecimal(speedRaw).divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP);
        } else {
            speed = new BigDecimal(0);
        }

        final int heartRateRaw = viewer.unsignedToInt(HR_OFFSET, 1);
        if (heartRateRaw != UNSIGNED_INT8_MAX) {
            heartRate = heartRateRaw;
        } else {
            heartRate = 0;
        }

        heartRateSource = Defines.HeartRateDataSource.getValueFromInt(HR_SOURCE_MASK & packet[META_OFFSET]);
        usingVirtualSpeed = booleanFromU8(packet[META_OFFSET], VIRTUAL_SPEED_MASK);

        type = Defines.EquipmentType.getValueFromInt(packet[TYPE_OFFSET] & TYPE_MASK);
        
    }

    private int getDeltaUnscaled(GeneralData old) {
        if (old.timeElapsed > timeElapsed) {
            return (timeElapsed - old.timeElapsed) + UNSIGNED_INT8_MAX + 1;
        }
        return timeElapsed - old.timeElapsed;
    }

    /**
     * Seconds elapsed since last update
     * @param old page containing previous update
     * @return time in seconds since last update
     */
    public BigDecimal getTimeDelta(GeneralData old) {
        if (old == null) {
            return scaleTime(timeElapsed);
        }
        return scaleTime(getDeltaUnscaled(old));
    }

    private BigDecimal scaleTime(int old) {
        return new BigDecimal(old).divide(new BigDecimal(4), 2, RoundingMode.HALF_UP);
    }

    /**
     * You should only store a previous update with isDistanceAvailable equal to true
     * @param old page containing previous update
     * @return distance in m
     */
    @Override
    public long getDistanceDelta(DistanceDecodable old) {
        if (old == null) {
            return distanceCovered;
        }
        return CounterUtils.calcDelta(UNSIGNED_INT8_MAX, old.getDistanceCovered(), distanceCovered);
    }

    // scale to km?
    private BigDecimal scaleDistance(long distance) {
        return new BigDecimal(distance).divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP);
    }

}
