package com.music.player.bhandari.m.equalizer;

/**
 * Copyright 2017 Amit Bhandari AB
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class EqualizerSetting {

    private int fiftyHertz = 16;
    private int oneThirtyHertz = 16;
    private int threeTwentyHertz = 16;
    private int eightHundredHertz = 16;
    private int twoKilohertz = 16;
    private int fiveKilohertz = 16;
    private int twelvePointFiveKilohertz = 16;
    private int virtualizer = 16;
    private int bassBoost = 16;
    private int enhancement = 16;
    private int reverb = 16;

    public EqualizerSetting(int fiftyHertz, int oneThirtyHertz, int threeTwentyHertz, int eightHundredHertz, int twoKilohertz, int fiveKilohertz, int twelvePointFiveKilohertz
            , int virtualizer, int bassBoost, int reverb) {
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

    public EqualizerSetting() {
    }

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

    public int getEnhancement() {
        return enhancement;
    }

    public void setBassBoost(int bassBoost) {
        this.bassBoost = bassBoost;
    }

    public void setEnhancement(int enhancement) {
        this.enhancement = enhancement;
    }

    public int getReverb() {
        return reverb;
    }

    public void setReverb(int reverb) {
        this.reverb = reverb;
    }
}
