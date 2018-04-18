package com.music.player.bhandari.m.equalizer;

/**
 * Created by Amit AB AB on 11/7/2017.
 */

public class EqualizerSetting {

    private int fiftyHertz=16;
    private int oneThirtyHertz=16;
    private int threeTwentyHertz=16;
    private int eightHundredHertz=16;
    private int twoKilohertz=16;
    private int fiveKilohertz=16;
    private int twelvePointFiveKilohertz=16;
    private int virtualizer=16;
    private int bassBoost=16;
    private int reverb=16;

    public EqualizerSetting(int fiftyHertz, int oneThirtyHertz, int threeTwentyHertz, int eightHundredHertz, int twoKilohertz, int fiveKilohertz, int twelvePointFiveKilohertz
    , int virtualizer, int bassBoost, int reverb){
        this.fiftyHertz = fiftyHertz;
        this.oneThirtyHertz = oneThirtyHertz;
        this.threeTwentyHertz = threeTwentyHertz;
        this.eightHundredHertz = eightHundredHertz;
        this.twoKilohertz = twoKilohertz;
        this.fiveKilohertz = fiveKilohertz;
        this.twelvePointFiveKilohertz = twelvePointFiveKilohertz;
        this.virtualizer = virtualizer;
        this.bassBoost = bassBoost;
        this.reverb = reverb;
    }

    public EqualizerSetting(){}

    @Override
    public String toString() {
        return "" + fiftyHertz + " : "
                + oneThirtyHertz + " : "
                + threeTwentyHertz + " : "
                + eightHundredHertz + " : "
                + twoKilohertz + " : "
                + fiveKilohertz + " : "
                + twelvePointFiveKilohertz + " : "
                + virtualizer + " : "
                + bassBoost + " : "
                + reverb + " : ";
    }

    public int getFiftyHertz() {
        return fiftyHertz;
    }

    public void setFiftyHertz(int fiftyHertz) {
        this.fiftyHertz = fiftyHertz;
    }

    public int getOneThirtyHertz() {
        return oneThirtyHertz;
    }

    public void setOneThirtyHertz(int oneThirtyHertz) {
        this.oneThirtyHertz = oneThirtyHertz;
    }

    public int getThreeTwentyHertz() {
        return threeTwentyHertz;
    }

    public void setThreeTwentyHertz(int threeTwentyHertz) {
        this.threeTwentyHertz = threeTwentyHertz;
    }

    public int getEightHundredHertz() {
        return eightHundredHertz;
    }

    public void setEightHundredHertz(int eightHundredHertz) {
        this.eightHundredHertz = eightHundredHertz;
    }

    public int getTwoKilohertz() {
        return twoKilohertz;
    }

    public void setTwoKilohertz(int twoKilohertz) {
        this.twoKilohertz = twoKilohertz;
    }

    public int getFiveKilohertz() {
        return fiveKilohertz;
    }

    public void setFiveKilohertz(int fiveKilohertz) {
        this.fiveKilohertz = fiveKilohertz;
    }

    public int getTwelvePointFiveKilohertz() {
        return twelvePointFiveKilohertz;
    }

    public void setTwelvePointFiveKilohertz(int twelvePointFiveKilohertz) {
        this.twelvePointFiveKilohertz = twelvePointFiveKilohertz;
    }

    public int getVirtualizer() {
        return virtualizer;
    }

    public void setVirtualizer(int virtualizer) {
        this.virtualizer = virtualizer;
    }

    public int getBassBoost() {
        return bassBoost;
    }

    public void setBassBoost(int bassBoost) {
        this.bassBoost = bassBoost;
    }

    public int getReverb() {
        return reverb;
    }

    public void setReverb(int reverb) {
        this.reverb = reverb;
    }
}
