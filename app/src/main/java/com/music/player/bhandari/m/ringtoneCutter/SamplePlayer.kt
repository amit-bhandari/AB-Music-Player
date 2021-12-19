/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.music.player.bhandari.m.ringtoneCutter

import com.music.player.bhandari.m.ringtoneCutter.soundfile.SoundFile

internal class SamplePlayer constructor(
    samples: ShortBuffer,
    sampleRate: Int,
    channels: Int,
    numSamples: Int
) {
    open interface OnCompletionListener {
        fun onCompletion()
    }

    private val mSamples: ShortBuffer
    private val mSampleRate: Int
    private val mChannels: Int
    private val mNumSamples // Number of samples per channel.
            : Int
    private val mAudioTrack: AudioTrack
    private val mBuffer: ShortArray
    private var mPlaybackStart // Start offset, in samples.
            : Int
    private var mPlayThread: Thread?
    private var mKeepPlaying: Boolean
    private var mListener: OnCompletionListener?

    constructor(sf: SoundFile?) : this(sf!!.getSamples(),
        sf!!.getSampleRate(),
        sf!!.getChannels(),
        sf!!.getNumSamples()) {
    }

    fun setOnCompletionListener(listener: OnCompletionListener?) {
        mListener = listener
    }

    fun isPlaying(): Boolean {
        return mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
    }

    fun isPaused(): Boolean {
        return mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED
    }

    fun start() {
        if (isPlaying()) {
            return
        }
        mKeepPlaying = true
        mAudioTrack.flush()
        mAudioTrack.play()
        // Setting thread feeding the audio samples to the audio hardware.
        // (Assumes mChannels = 1 or 2).
        mPlayThread = object : Thread() {
            public override fun run() {
                val position: Int = mPlaybackStart * mChannels
                mSamples.position(position)
                val limit: Int = mNumSamples * mChannels
                while (mSamples.position() < limit && mKeepPlaying) {
                    val numSamplesLeft: Int = limit - mSamples.position()
                    if (numSamplesLeft >= mBuffer.size) {
                        mSamples.get(mBuffer)
                    } else {
                        for (i in numSamplesLeft until mBuffer.size) {
                            mBuffer.get(i) = 0
                        }
                        mSamples.get(mBuffer, 0, numSamplesLeft)
                    }
                    // TODO(nfaralli): use the write method that takes a ByteBuffer as argument.
                    mAudioTrack.write(mBuffer, 0, mBuffer.size)
                }
            }
        }
        mPlayThread.start()
    }

    fun pause() {
        if (isPlaying()) {
            mAudioTrack.pause()
            // mAudioTrack.write() should block if it cannot write.
        }
    }

    fun stop() {
        if (isPlaying() || isPaused()) {
            mKeepPlaying = false
            mAudioTrack.pause() // pause() stops the playback immediately.
            mAudioTrack.stop() // Unblock mAudioTrack.write() to avoid deadlocks.
            if (mPlayThread != null) {
                try {
                    mPlayThread!!.join()
                } catch (e: InterruptedException) {
                }
                mPlayThread = null
            }
            mAudioTrack.flush() // just in case...
        }
    }

    fun release() {
        stop()
        mAudioTrack.release()
    }

    fun seekTo(msec: Int) {
        val wasPlaying: Boolean = isPlaying()
        stop()
        mPlaybackStart = (msec * (mSampleRate / 1000.0)).toInt()
        if (mPlaybackStart > mNumSamples) {
            mPlaybackStart = mNumSamples // Nothing to play...
        }
        mAudioTrack.setNotificationMarkerPosition(mNumSamples - 1 - mPlaybackStart)
        if (wasPlaying) {
            start()
        }
    }

    fun getCurrentPosition(): Int {
        return ((mPlaybackStart + mAudioTrack.getPlaybackHeadPosition()) *
                (1000.0 / mSampleRate))
    }

    init {
        mSamples = samples
        mSampleRate = sampleRate
        mChannels = channels
        mNumSamples = numSamples
        mPlaybackStart = 0
        var bufferSize: Int = AudioTrack.getMinBufferSize(
            mSampleRate,
            if (mChannels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT)
        // make sure minBufferSize can contain at least 1 second of audio (16 bits sample).
        if (bufferSize < mChannels * mSampleRate * 2) {
            bufferSize = mChannels * mSampleRate * 2
        }
        mBuffer = ShortArray(bufferSize / 2) // bufferSize is in Bytes.
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            mSampleRate,
            if (mChannels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            mBuffer.size * 2,
            AudioTrack.MODE_STREAM)
        // Check when player played all the given data and notify user if mListener is set.
        mAudioTrack.setNotificationMarkerPosition(mNumSamples - 1) // Set the marker to the end.
        mAudioTrack.setPlaybackPositionUpdateListener(
            object : AudioTrack.OnPlaybackPositionUpdateListener {
                public override fun onPeriodicNotification(track: AudioTrack) {}
                public override fun onMarkerReached(track: AudioTrack) {
                    stop()
                    if (mListener != null) {
                        mListener!!.onCompletion()
                    }
                }
            })
        mPlayThread = null
        mKeepPlaying = true
        mListener = null
    }
}