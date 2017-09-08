package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.BitManipulation;
import org.cowboycoders.ant.profiles.pages.AntPacketEncodable;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.fluxoid.utils.bytes.LittleEndianArray;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * p51
 * Created by fluxoid on 16/01/17.
 */
public class TrackResistance implements AntPage {

    public static final int PAGE_NUMBER = 51;

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    // if you change these you have to adjust scale in encode()
    private static final BigDecimal DEFAULT_ROLLING_RESISTANCE = new BigDecimal(0.004);
    private static final BigDecimal DEFAULT_GRADIENT = new BigDecimal(0.00);

    private static final int GRADIENT_OFFSET = 5;
    private static final int ROLLING_OFFSET = 7;
    private final BigDecimal gradient;
    private final BigDecimal coefficientRollingResistance;

    public static class TrackResistancePayload implements AntPacketEncodable {
        private BigDecimal gradient = DEFAULT_GRADIENT;
        private BigDecimal coefficientRollingResistance = DEFAULT_ROLLING_RESISTANCE;

        public BigDecimal getGradient() {
            return gradient;
        }

        public static TrackResistancePayload from(TrackResistance old) {
            TrackResistancePayload payload = new TrackResistancePayload();
            payload.setGradient(old.getGradient());
            payload.setCoefficientRollingResistance(old.getCoefficientRollingResistance());
            return payload;
        }

        public TrackResistancePayload setGradient(BigDecimal gradient) {
            if (gradient == null) {
                throw new IllegalArgumentException("gradient cannot be null");
            }
            this.gradient = gradient;
            return this;
        }

        public BigDecimal getCoefficientRollingResistance() {
            return coefficientRollingResistance;
        }

        public TrackResistancePayload setCoefficientRollingResistance(BigDecimal coefficientRollingResistance) {
            if (coefficientRollingResistance == null) {
                throw new IllegalArgumentException("coefficient cannot be null");
            }
            this.coefficientRollingResistance = coefficientRollingResistance;
            return this;
        }

        public void encode(final byte[] packet) {
            LittleEndianArray viewer = new LittleEndianArray(packet);
            viewer.putUnsigned(PAGE_OFFSET, 1, PAGE_NUMBER);
            if (gradient.setScale(2, RoundingMode.HALF_UP).equals(DEFAULT_GRADIENT)) {
                viewer.putUnsigned(GRADIENT_OFFSET,2, UNSIGNED_INT16_MAX);
            } else {
                BigDecimal raw = gradient.add(new BigDecimal(200))
                        .multiply(new BigDecimal(100))
                        .setScale(0, RoundingMode.HALF_UP);
                viewer.putUnsigned(GRADIENT_OFFSET,2, raw.intValue());

            }
            if (coefficientRollingResistance.setScale(3, RoundingMode.HALF_UP).equals(DEFAULT_ROLLING_RESISTANCE)) {
                viewer.putUnsigned(ROLLING_OFFSET, 1, UNSIGNED_INT8_MAX);
            } else {
                BigDecimal raw = coefficientRollingResistance.multiply(new BigDecimal(20000))
                        .setScale(0, RoundingMode.HALF_UP);
                viewer.putUnsigned(ROLLING_OFFSET, 1, raw.intValue());

            }


        }

        public TrackResistance createTrackResistance() {
            final byte [] packet = new byte[8];
            this.encode(packet);
            return new TrackResistance(packet);
        }
    }

    public TrackResistance(byte[] packet) {
        LittleEndianArray viewer = new LittleEndianArray(packet);
        int gradRaw = viewer.unsignedToInt(GRADIENT_OFFSET, 2);
        if (gradRaw != BitManipulation.UNSIGNED_INT16_MAX) {
            gradient = new BigDecimal(gradRaw).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                    .subtract(new BigDecimal(200));
        } else {
            gradient = DEFAULT_GRADIENT;
        }
        int rollingRaw = viewer.unsignedToInt(ROLLING_OFFSET, 1);
        if (rollingRaw != UNSIGNED_INT8_MAX) {
            coefficientRollingResistance = new BigDecimal(rollingRaw)
                    .divide(new BigDecimal(20000), 5, RoundingMode.HALF_UP);
        } else {
            coefficientRollingResistance = DEFAULT_ROLLING_RESISTANCE;
        }


    }

    /**
     * slope of the terrain in range -200% to 200%, resolution 0.01
     */
    public BigDecimal getGradient() {
        return gradient;
    }

    /**
     * Used to calculate resistance due to friction. In range: 0 - 0.0127, resolution: 5x10^-5
     * @return
     */
    public BigDecimal getCoefficientRollingResistance() {
        return coefficientRollingResistance;
    }
}
