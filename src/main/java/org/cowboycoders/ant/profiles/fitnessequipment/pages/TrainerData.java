package org.cowboycoders.ant.profiles.fitnessequipment.pages;

import org.cowboycoders.ant.profiles.TimeOutDeltaValidator;
import org.cowboycoders.ant.profiles.common.utils.CounterUtils;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CounterBasedDecodable;
import org.cowboycoders.ant.profiles.common.decode.interfaces.PowerOnlyDecodable;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.cowboycoders.ant.profiles.BitManipulation.*;

/**
 * Created by fluxoid on 02/01/17.
 */
public class TrainerData extends CommonPageData implements PowerOnlyDecodable {

    private static final long TIMEOUT_DELTA = TimeUnit.SECONDS.toNanos(12);
    public static final int POWER_OFFSET = 4;
    public static final int EVENT_OFFSET = 2;
    public static final int INSTANT_POWER_OFFSET = 6;
    public static final int CADENCE_OFFSET = 3;

    private final int power;
    private final int instantPower;
    private final boolean powerAvailable;
    private final int events;
    private final long timestamp;

    /**
     * Accumulated power : running sum of instanteous power updated on each increment of event count
     * @return
     */
    public int getSumPower() {
        return power;
    }

    public int getInstantPower() {
        return instantPower;
    }

    public boolean isPowerAvailable() {
        return powerAvailable;
    }

    /**
     *
     * @return
     */
    public int getEventCount() {
        return events;
    }

    @Override
    public boolean isValidDelta(CounterBasedDecodable old) {
        return timeOutDeltaValidator.isValidDelta(old, this);
    }

    /**
     * @return in rpm
     */
    public Integer getCadence() {
        return cadence;
    }

    public EnumSet<Defines.TrainerStatusFlag> getTrainerStatus() {
        return trainerStatus;
    }

    private final Integer cadence;
    private final EnumSet<Defines.TrainerStatusFlag> trainerStatus;
    private final TimeOutDeltaValidator timeOutDeltaValidator = new TimeOutDeltaValidator(TIMEOUT_DELTA);


    public TrainerData(byte [] packet) {
        super(packet);
        this.timestamp = System.nanoTime();
        power = UnsignedNumFrom2LeBytes(packet, POWER_OFFSET);
        events = UnsignedNumFrom1LeByte(packet[EVENT_OFFSET]);
        instantPower = 0xfff & UnsignedNumFrom2LeBytes(packet, INSTANT_POWER_OFFSET);
        if (instantPower != UNSIGNED_INT12_MAX) {
            powerAvailable = true;
        } else {
            powerAvailable = false;
        }
        final int cadenceRaw = UnsignedNumFrom1LeByte(packet[CADENCE_OFFSET]);
        if (cadenceRaw != UNSIGNED_INT8_MAX) {
            cadence = cadenceRaw;
        } else {
            cadence = null;
        }
        trainerStatus = Defines.TrainerStatusFlag.getEnumSet(packet);

    }


    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getSumPowerDelta(PowerOnlyDecodable old) {
        return CounterUtils.calcDelta(old.getSumPower(), getSumPower(), UNSIGNED_INT16_MAX);
    }

    @Override
    public long getEventCountDelta(CounterBasedDecodable old) {
        return CounterUtils.calcDelta(old.getEventCount(), getEventCount(), UNSIGNED_INT8_MAX);
    }


}