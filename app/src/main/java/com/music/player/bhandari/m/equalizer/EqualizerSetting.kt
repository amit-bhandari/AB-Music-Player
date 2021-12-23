package com.music.player.bhandari.m.equalizer

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class EqualizerSetting {
    private var fiftyHertz: Int = 16
    private var oneThirtyHertz: Int = 16
    private var threeTwentyHertz: Int = 16
    private var eightHundredHertz: Int = 16
    private var twoKilohertz: Int = 16
    private var fiveKilohertz: Int = 16
    private var twelvePointFiveKilohertz: Int = 16
    private var virtualizer: Int = 16
    private var bassBoost: Int = 16
    private var enhancement: Int = 16
    private var reverb: Int = 16

    constructor(
        fiftyHertz: Int,
        oneThirtyHertz: Int,
        threeTwentyHertz: Int,
        eightHundredHertz: Int,
        twoKilohertz: Int,
        fiveKilohertz: Int,
        twelvePointFiveKilohertz: Int,
        virtualizer: Int,
        bassBoost: Int,
        reverb: Int
    ) {
        this.fiftyHertz = fiftyHertz
        this.oneThirtyHertz = oneThirtyHertz
        this.threeTwentyHertz = threeTwentyHertz
        this.eightHundredHertz = eightHundredHertz
        this.twoKilohertz = twoKilohertz
        this.fiveKilohertz = fiveKilohertz
        this.twelvePointFiveKilohertz = twelvePointFiveKilohertz
        this.virtualizer = virtualizer
        this.bassBoost = bassBoost
        this.reverb = reverb
    }

    constructor()

    override fun toString(): String {
        return ("" + fiftyHertz + " : "
                + oneThirtyHertz + " : "
                + threeTwentyHertz + " : "
                + eightHundredHertz + " : "
                + twoKilohertz + " : "
                + fiveKilohertz + " : "
                + twelvePointFiveKilohertz + " : "
                + virtualizer + " : "
                + bassBoost + " : "
                + reverb + " : ")
    }

    fun getFiftyHertz(): Int {
        return fiftyHertz
    }

    fun setFiftyHertz(fiftyHertz: Int) {
        this.fiftyHertz = fiftyHertz
    }

    fun getOneThirtyHertz(): Int {
        return oneThirtyHertz
    }

    fun setOneThirtyHertz(oneThirtyHertz: Int) {
        this.oneThirtyHertz = oneThirtyHertz
    }

    fun getThreeTwentyHertz(): Int {
        return threeTwentyHertz
    }

    fun setThreeTwentyHertz(threeTwentyHertz: Int) {
        this.threeTwentyHertz = threeTwentyHertz
    }

    fun getEightHundredHertz(): Int {
        return eightHundredHertz
    }

    fun setEightHundredHertz(eightHundredHertz: Int) {
        this.eightHundredHertz = eightHundredHertz
    }

    fun getTwoKilohertz(): Int {
        return twoKilohertz
    }

    fun setTwoKilohertz(twoKilohertz: Int) {
        this.twoKilohertz = twoKilohertz
    }

    fun getFiveKilohertz(): Int {
        return fiveKilohertz
    }

    fun setFiveKilohertz(fiveKilohertz: Int) {
        this.fiveKilohertz = fiveKilohertz
    }

    fun getTwelvePointFiveKilohertz(): Int {
        return twelvePointFiveKilohertz
    }

    fun setTwelvePointFiveKilohertz(twelvePointFiveKilohertz: Int) {
        this.twelvePointFiveKilohertz = twelvePointFiveKilohertz
    }

    fun getVirtualizer(): Int {
        return virtualizer
    }

    fun setVirtualizer(virtualizer: Int) {
        this.virtualizer = virtualizer
    }

    fun getBassBoost(): Int {
        return bassBoost
    }

    fun getEnhancement(): Int {
        return enhancement
    }

    fun setBassBoost(bassBoost: Int) {
        this.bassBoost = bassBoost
    }

    fun setEnhancement(enhancement: Int) {
        this.enhancement = enhancement
    }

    fun getReverb(): Int {
        return reverb
    }

    fun setReverb(reverb: Int) {
        this.reverb = reverb
    }
}