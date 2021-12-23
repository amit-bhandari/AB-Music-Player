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
package com.music.player.bhandari.m.ringtoneCutter.soundfile

import kotlin.experimental.or

internal class Atom {
    // note: latest versions of spec simply call it 'box' instead of 'atom'.
    private var mSize // includes atom header (8 bytes)
            : Int
    private var mType: Int
    private var mData // an atom can either contain data or children, but not both.
            : ByteArray?
    private var mChildren: Array<Atom?>?
    private var mVersion // if negative, then the atom does not contain version and flags data.
            : Byte
    private var mFlags: Int

    // create an empty atom of the given type.
    constructor(type: String) {
        mSize = 8
        mType = getTypeInt(type)
        mData = null
        mChildren = null
        mVersion = -1
        mFlags = 0
    }

    // create an empty atom of type type, with a given version and flags.
    constructor(type: String, version: Byte, flags: Int) {
        mSize = 12
        mType = getTypeInt(type)
        mData = null
        mChildren = null
        mVersion = version
        mFlags = flags
    }

    // set the size field of the atom based on its content.
    private fun setSize() {
        var size: Int = 8 // type + size
        if (mVersion >= 0) {
            size += 4 // version + flags
        }
        if (mData != null) {
            size += mData!!.size
        } else if (mChildren != null) {
            for (child: Atom? in mChildren!!) {
                size += child!!.getSize()
            }
        }
        mSize = size
    }

    // get the size of the this atom.
    fun getSize(): Int {
        return mSize
    }

    private fun getTypeInt(type_str: String): Int {
        var type = 0
        type = type or ((type_str[0]).code shl 24)
        type = type or ((type_str[1]).code shl 16)
        type = type or ((type_str[2]).code shl 8)
        type = type or (type_str[3]).code.toByte().toInt()
        return type
    }

    fun getTypeInt(): Int {
        return mType
    }

    fun getTypeStr(): String {
        var type = ""
        type += (((mType shr 24) and 0xFF).toByte()).toInt().toChar()
        type += (((mType shr 16) and 0xFF).toByte()).toInt().toChar()
        type += (((mType shr 8) and 0xFF).toByte()).toInt().toChar()
        type += ((mType and 0xFF).toByte()).toInt().toChar()
        return type
    }

    fun setData(data: ByteArray?): Boolean {
        if (mChildren != null || data == null) {
            // TODO(nfaralli): log something here
            return false
        }
        mData = data
        setSize()
        return true
    }

    fun getData(): ByteArray? {
        return mData
    }

    fun addChild(child: Atom?): Boolean {
        if (mData != null || child == null) {
            // TODO(nfaralli): log something here
            return false
        }
        var numChildren = 1
        if (mChildren != null) {
            numChildren += mChildren!!.size
        }
        val children: Array<Atom?> = arrayOfNulls(numChildren)
        if (mChildren != null) {
            System.arraycopy(mChildren, 0, children, 0, mChildren!!.size)
        }
        children[numChildren - 1] = child
        mChildren = children
        setSize()
        return true
    }

    // return the child atom of the corresponding type.
    // type can contain grand children: e.g. type = "trak.mdia.minf"
    // return null if the atom does not contain such a child.
    fun getChild(type: String): Atom? {
        if (mChildren == null) {
            return null
        }
        val types: Array<String> = type.split("\\.".toRegex(), 2).toTypedArray()
        for (child: Atom? in mChildren!!) {
            if ((child!!.getTypeStr() == types.get(0))) {
                if (types.size == 1) {
                    return child
                } else {
                    return child.getChild(types.get(1))
                }
            }
        }
        return null
    }

    // return a byte array containing the full content of the atom (including header)
    fun getBytes(): ByteArray {
        val atom_bytes: ByteArray = ByteArray(mSize)
        var offset: Int = 0
        atom_bytes[offset++] = ((mSize shr 24) and 0xFF).toByte()
        atom_bytes[offset++] = ((mSize shr 16) and 0xFF).toByte()
        atom_bytes[offset++] = ((mSize shr 8) and 0xFF).toByte()
        atom_bytes[offset++] = (mSize and 0xFF).toByte()
        atom_bytes[offset++] = ((mType shr 24) and 0xFF).toByte()
        atom_bytes[offset++] = ((mType shr 16) and 0xFF).toByte()
        atom_bytes[offset++] = ((mType shr 8) and 0xFF).toByte()
        atom_bytes[offset++] = (mType and 0xFF).toByte()
        if (mVersion >= 0) {
            atom_bytes[offset++] = mVersion
            atom_bytes[offset++] = ((mFlags shr 16) and 0xFF).toByte()
            atom_bytes[offset++] = ((mFlags shr 8) and 0xFF).toByte()
            atom_bytes[offset++] = (mFlags and 0xFF).toByte()
        }
        if (mData != null) {
            System.arraycopy(mData, 0, atom_bytes, offset, mData!!.size)
        } else if (mChildren != null) {
            var child_bytes: ByteArray
            for (child: Atom? in mChildren!!) {
                child_bytes = child!!.getBytes()
                System.arraycopy(child_bytes, 0, atom_bytes, offset, child_bytes.size)
                offset += child_bytes.size
            }
        }
        return atom_bytes
    }

    // Used for debugging purpose only.
    override fun toString(): String {
        var str: String = ""
        val atom_bytes: ByteArray = getBytes()
        for (i in atom_bytes.indices) {
            if (i % 8 == 0 && i > 0) {
                str += '\n'
            }
            str += String.format("0x%02X", atom_bytes.get(i))
            if (i < atom_bytes.size - 1) {
                str += ','
                if (i % 8 < 7) {
                    str += ' '
                }
            }
        }
        str += '\n'
        return str
    }
}

class MP4Header constructor(
    sampleRate: Int,
    numChannels: Int,
    frame_size: IntArray?,
    bitrate: Int
) {
    private val mFrameSize // size of each AAC frames, in bytes. First one should be 2.
            : IntArray?
    private var mMaxFrameSize // size of the biggest frame.
            : Int
    private var mTotSize // size of the AAC stream.
            : Int
    private val mBitrate // bitrate used to encode the AAC stream.
            : Int
    private val mTime // time used for 'creation time' and 'modification time' fields.
            : ByteArray
    private val mDurationMS // duration of stream in milliseconds.
            : ByteArray
    private val mNumSamples // number of samples in the stream.
            : ByteArray
    private var mHeader // the complete header.
            : ByteArray? = null
    private val mSampleRate // sampling frequency in Hz (e.g. 44100).
            : Int
    private val mChannels // number of channels.
            : Int

    fun getMP4Header(): ByteArray? {
        return mHeader
    }

    override fun toString(): String {
        var str: String = ""
        if (mHeader == null) {
            return str
        }
        val num_32bits_per_lines: Int = 8
        for ((count, b: Byte) in mHeader!!.withIndex()) {
            val break_line: Boolean = count > 0 && count % (num_32bits_per_lines * 4) == 0
            val insert_space: Boolean = (count > 0) && (count % 4 == 0) && !break_line
            if (break_line) {
                str += '\n'
            }
            if (insert_space) {
                str += ' '
            }
            str += String.format("%02X", b)
        }
        return str
    }

    private fun setHeader() {
        // create the atoms needed to build the header.
        val a_ftyp: Atom = getFTYPAtom()
        val a_moov: Atom = getMOOVAtom()
        val a_mdat = Atom("mdat") // create an empty atom. The AAC stream data should follow
        // immediately after. The correct size will be set later.

        // set the correct chunk offset in the stco atom.
        val a_stco: Atom? = a_moov.getChild("trak.mdia.minf.stbl.stco")
        if (a_stco == null) {
            mHeader = null
            return
        }
        val data: ByteArray? = a_stco.getData()
        val chunk_offset: Int = a_ftyp.getSize() + a_moov.getSize() + a_mdat.getSize()
        var offset: Int = data!!.size - 4 // here stco should contain only one chunk offset.
        data[offset++] = ((chunk_offset shr 24) and 0xFF).toByte()
        data[offset++] = ((chunk_offset shr 16) and 0xFF).toByte()
        data[offset++] = ((chunk_offset shr 8) and 0xFF).toByte()
        data[offset++] = (chunk_offset and 0xFF).toByte()

        // create the header byte array based on the previous atoms.
        val header: ByteArray =
            ByteArray(chunk_offset) // here chunk_offset is also the size of the header
        offset = 0
        for (atom: Atom in arrayOf(a_ftyp, a_moov, a_mdat)) {
            val atom_bytes: ByteArray = atom.getBytes()
            System.arraycopy(atom_bytes, 0, header, offset, atom_bytes.size)
            offset += atom_bytes.size
        }

        //set the correct size of the mdat atom
        val size: Int = 8 + mTotSize
        offset -= 8
        header[offset++] = ((size shr 24) and 0xFF).toByte()
        header[offset++] = ((size shr 16) and 0xFF).toByte()
        header[offset++] = ((size shr 8) and 0xFF).toByte()
        header[offset++] = (size and 0xFF).toByte()
        mHeader = header
    }

    private fun getFTYPAtom(): Atom {
        val atom = Atom("ftyp")
        atom.setData(byteArrayOf(
            'M'.code.toByte(), '4'.code.toByte(), 'A'.code.toByte(), ' '.code.toByte(),  // Major brand
            0, 0, 0, 0,  // Minor version
            'M'.code.toByte(), '4'.code.toByte(), 'A'.code.toByte(), ' '.code.toByte(),  // compatible brands
            'm'.code.toByte(), 'p'.code.toByte(), '4'.code.toByte(), '2'.code.toByte(),
            'i'.code.toByte(), 's'.code.toByte(), 'o'.code.toByte(), 'm'
                .code.toByte()))
        return atom
    }

    private fun getMOOVAtom(): Atom {
        val atom = Atom("moov")
        atom.addChild(getMVHDAtom())
        atom.addChild(getTRAKAtom())
        return atom
    }

    private fun getMVHDAtom(): Atom {
        val atom = Atom("mvhd", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            mTime[0],
            mTime[1],
            mTime[2],
            mTime[3],  // creation time.
            mTime[0],
            mTime[1],
            mTime[2],
            mTime[3],  // modification time.
            0,
            0,
            0x03,
            0xE8.toByte(),  // timescale = 1000 => duration expressed in ms.
            mDurationMS[0],
            mDurationMS[1],
            mDurationMS[2],
            mDurationMS[3],  // duration in ms.
            0,
            1,
            0,
            0,  // rate = 1.0
            1,
            0,  // volume = 1.0
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            0,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,  // unity matrix
            0,
            0,
            0,
            0,
            0,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0x40,
            0,
            0,
            0,
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            2 // next track ID
        ))
        return atom
    }

    private fun getTRAKAtom(): Atom {
        val atom = Atom("trak")
        atom.addChild(getTKHDAtom())
        atom.addChild(getMDIAAtom())
        return atom
    }

    private fun getTKHDAtom(): Atom {
        val atom = Atom("tkhd", 0.toByte(), 0x07) // track enabled, in movie, and in preview.
        atom.setData(byteArrayOf(
            mTime[0],
            mTime[1],
            mTime[2],
            mTime[3],  // creation time.
            mTime[0],
            mTime[1],
            mTime[2],
            mTime[3],  // modification time.
            0,
            0,
            0,
            1,  // track ID
            0,
            0,
            0,
            0,  // reserved
            mDurationMS.get(0),
            mDurationMS.get(1),
            mDurationMS.get(2),
            mDurationMS.get(3),  // duration in ms.
            0,
            0,
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            0,
            0,  // layer
            0,
            0,  // alternate group
            1,
            0,  // volume = 1.0
            0,
            0,  // reserved
            0,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,  // unity matrix
            0,
            0,
            0,
            0,
            0,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0x40,
            0,
            0,
            0,
            0,
            0,
            0,
            0,  // width
            0,
            0,
            0,
            0 // height
        ))
        return atom
    }

    private fun getMDIAAtom(): Atom {
        val atom = Atom("mdia")
        atom.addChild(getMDHDAtom())
        atom.addChild(getHDLRAtom())
        atom.addChild(getMINFAtom())
        return atom
    }

    private fun getMDHDAtom(): Atom {
        val atom = Atom("mdhd", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            mTime.get(0),
            mTime.get(1),
            mTime.get(2),
            mTime.get(3),  // creation time.
            mTime.get(0),
            mTime.get(1),
            mTime.get(2),
            mTime.get(3),  // modification time.
            (mSampleRate shr 24).toByte(),
            (mSampleRate shr 16).toByte(),  // timescale = Fs =>
            (mSampleRate shr 8).toByte(),
            (mSampleRate).toByte(),  // duration expressed in samples.
            mNumSamples.get(0),
            mNumSamples.get(1),
            mNumSamples.get(2),
            mNumSamples.get(3),  // duration
            0,
            0,  // languages
            0,
            0 // pre-defined
        ))
        return atom
    }

    private fun getHDLRAtom(): Atom {
        val atom: Atom = Atom("hdlr", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            0,
            0,
            0,
            0,  // pre-defined
            's'.code.toByte(),
            'o'.code.toByte(),
            'u'.code.toByte(),
            'n'.code.toByte(),  // handler type
            0,
            0,
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            'S'.code.toByte(),
            'o'.code.toByte(),
            'u'.code.toByte(),
            'n'.code.toByte(),  // name (used only for debugging and inspection purposes).
            'd'.code.toByte(),
            'H'.code.toByte(),
            'a'.code.toByte(),
            'n'.code.toByte(),
            'd'.code.toByte(),
            'l'.code.toByte(),
            'e'.code.toByte(),
            '\u0000'
                .code.toByte()))
        return atom
    }

    private fun getMINFAtom(): Atom {
        val atom: Atom = Atom("minf")
        atom.addChild(getSMHDAtom())
        atom.addChild(getDINFAtom())
        atom.addChild(getSTBLAtom())
        return atom
    }

    private fun getSMHDAtom(): Atom {
        val atom: Atom = Atom("smhd", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            0, 0,  // balance (center)
            0, 0 // reserved
        ))
        return atom
    }

    private fun getDINFAtom(): Atom {
        val atom = Atom("dinf")
        atom.addChild(getDREFAtom())
        return atom
    }

    private fun getDREFAtom(): Atom {
        val atom = Atom("dref", 0.toByte(), 0)
        val url: ByteArray = getURLAtom().getBytes()
        val data = ByteArray(4 + url.size)
        data[3] = 0x01 // entry count = 1
        System.arraycopy(url, 0, data, 4, url.size)
        atom.setData(data)
        return atom
    }

    private fun getURLAtom(): Atom {
        return Atom("url ", 0.toByte(), 0x01) // flags = 0x01: data is self contained.
    }

    private fun getSTBLAtom(): Atom {
        val atom = Atom("stbl")
        atom.addChild(getSTSDAtom())
        atom.addChild(getSTTSAtom())
        atom.addChild(getSTSCAtom())
        atom.addChild(getSTSZAtom())
        atom.addChild(getSTCOAtom())
        return atom
    }

    private fun getSTSDAtom(): Atom {
        val atom = Atom("stsd", 0.toByte(), 0)
        val mp4a: ByteArray = getMP4AAtom().getBytes()
        val data = ByteArray(4 + mp4a.size)
        data[3] = 0x01 // entry count = 1
        System.arraycopy(mp4a, 0, data, 4, mp4a.size)
        atom.setData(data)
        return atom
    }

    // See also Part 14 section 5.6.1 of ISO/IEC 14496 for this atom.
    private fun getMP4AAtom(): Atom {
        val atom = Atom("mp4a")
        val ase: ByteArray = byteArrayOf( // Audio Sample Entry data
            0, 0, 0, 0, 0, 0,  // reserved
            0, 1,  // data reference index
            0, 0, 0, 0,  // reserved
            0, 0, 0, 0,  // reserved
            (mChannels shr 8).toByte(), mChannels.toByte(),  // channel count
            0, 0x10,  // sample size
            0, 0,  // pre-defined
            0, 0,  // reserved
            (mSampleRate shr 8).toByte(), (mSampleRate).toByte(), 0, 0)
        val esds: ByteArray = getESDSAtom().getBytes()
        val data = ByteArray(ase.size + esds.size)
        System.arraycopy(ase, 0, data, 0, ase.size)
        System.arraycopy(esds, 0, data, ase.size, esds.size)
        atom.setData(data)
        return atom
    }

    private fun getESDSAtom(): Atom {
        val atom = Atom("esds", 0.toByte(), 0)
        atom.setData(getESDescriptor())
        return atom
    }

    // Returns an ES Descriptor for an ISO/IEC 14496-3 audio stream, AAC LC, 44100Hz, 2 channels,
    // 1024 samples per frame per channel. The decoder buffer size is set so that it can contain at
    // least 2 frames. (See section 7.2.6.5 of ISO/IEC 14496-1 for more details).
    private fun getESDescriptor(): ByteArray {
        val samplingFrequencies: IntArray =
            intArrayOf(96000, 88200, 64000, 48000, 44100, 32000, 24000,
                22050, 16000, 12000, 11025, 8000, 7350)
        // First 5 bytes of the ES Descriptor.
        val ESDescriptor_top: ByteArray = byteArrayOf(0x03, 0x19, 0x00, 0x00, 0x00)
        // First 4 bytes of Decoder Configuration Descriptor. Audio ISO/IEC 14496-3, AudioStream.
        val decConfigDescr_top: ByteArray = byteArrayOf(0x04, 0x11, 0x40, 0x15)
        // Audio Specific Configuration: AAC LC, 1024 samples/frame/channel.
        // Sampling frequency and channels configuration are not set yet.
        val audioSpecificConfig: ByteArray = byteArrayOf(0x05, 0x02, 0x10, 0x00)
        val slConfigDescr: ByteArray = byteArrayOf(0x06, 0x01, 0x02) // specific for MP4 file.
        var bufferSize: Int = 0x300
        while (bufferSize < 2 * mMaxFrameSize) {
            // TODO(nfaralli): what should be the minimum size of the decoder buffer?
            // Should it be a multiple of 256?
            bufferSize += 0x100
        }

        // create the Decoder Configuration Descriptor
        val decConfigDescr = ByteArray(2 + decConfigDescr_top.get(1))
        System.arraycopy(decConfigDescr_top, 0, decConfigDescr, 0, decConfigDescr_top.size)
        var offset: Int = decConfigDescr_top.size
        decConfigDescr[offset++] = ((bufferSize shr 16) and 0xFF).toByte()
        decConfigDescr[offset++] = ((bufferSize shr 8) and 0xFF).toByte()
        decConfigDescr[offset++] = (bufferSize and 0xFF).toByte()
        decConfigDescr[offset++] = ((mBitrate shr 24) and 0xFF).toByte()
        decConfigDescr[offset++] = ((mBitrate shr 16) and 0xFF).toByte()
        decConfigDescr[offset++] = ((mBitrate shr 8) and 0xFF).toByte()
        decConfigDescr[offset++] = (mBitrate and 0xFF).toByte()
        decConfigDescr[offset++] = ((mBitrate shr 24) and 0xFF).toByte()
        decConfigDescr[offset++] = ((mBitrate shr 16) and 0xFF).toByte()
        decConfigDescr[offset++] = ((mBitrate shr 8) and 0xFF).toByte()
        decConfigDescr[offset++] = (mBitrate and 0xFF).toByte()
        var index: Int
        index = 0
        while (index < samplingFrequencies.size) {
            if (samplingFrequencies.get(index) == mSampleRate) {
                break
            }
            index++
        }
        if (index == samplingFrequencies.size) {
            // TODO(nfaralli): log something here.
            // Invalid sampling frequency. Default to 44100Hz...
            index = 4
        }
        audioSpecificConfig[2] = audioSpecificConfig[2] or ((index shr 1) and 0x07).toByte()
        audioSpecificConfig[3] =
            audioSpecificConfig[3] or (((index and 1) shl 7) or ((mChannels and 0x0F) shl 3)).toByte()
        System.arraycopy(
            audioSpecificConfig, 0, decConfigDescr, offset, audioSpecificConfig.size)

        // create the ES Descriptor
        val ESDescriptor = ByteArray(2 + ESDescriptor_top.get(1))
        System.arraycopy(ESDescriptor_top, 0, ESDescriptor, 0, ESDescriptor_top.size)
        offset = ESDescriptor_top.size
        System.arraycopy(decConfigDescr, 0, ESDescriptor, offset, decConfigDescr.size)
        offset += decConfigDescr.size
        System.arraycopy(slConfigDescr, 0, ESDescriptor, offset, slConfigDescr.size)
        return ESDescriptor
    }

    private fun getSTTSAtom(): Atom {
        val atom = Atom("stts", 0.toByte(), 0)
        val numAudioFrames: Int = mFrameSize!!.size - 1
        atom.setData(byteArrayOf(
            0,
            0,
            0,
            0x02,  // entry count
            0,
            0,
            0,
            0x01,  // first frame contains no audio
            0,
            0,
            0,
            0,
            ((numAudioFrames shr 24) and 0xFF).toByte(),
            ((numAudioFrames shr 16) and 0xFF).toByte(),
            ((numAudioFrames shr 8) and 0xFF).toByte(),
            (numAudioFrames and 0xFF).toByte(),
            0,
            0,
            0x04,
            0))
        return atom
    }

    private fun getSTSCAtom(): Atom {
        val atom: Atom = Atom("stsc", 0.toByte(), 0)
        val numFrames: Int = mFrameSize!!.size
        atom.setData(byteArrayOf(
            0,
            0,
            0,
            0x01,  // entry count
            0,
            0,
            0,
            0x01,  // first chunk
            ((numFrames shr 24) and 0xFF).toByte(),
            ((numFrames shr 16) and 0xFF).toByte(),  // samples per
            ((numFrames shr 8) and 0xFF).toByte(),
            (numFrames and 0xFF).toByte(),  // chunk
            0,
            0,
            0,
            0x01))
        return atom
    }

    private fun getSTSZAtom(): Atom {
        val atom = Atom("stsz", 0.toByte(), 0)
        val numFrames: Int = mFrameSize!!.size
        val data = ByteArray(8 + 4 * numFrames)
        var offset = 0
        data[offset++] = 0 // sample size (=0 => each frame can have a different size)
        data[offset++] = 0
        data[offset++] = 0
        data[offset++] = 0
        data[offset++] = ((numFrames shr 24) and 0xFF).toByte() // sample count
        data[offset++] = ((numFrames shr 16) and 0xFF).toByte()
        data[offset++] = ((numFrames shr 8) and 0xFF).toByte()
        data[offset++] = (numFrames and 0xFF).toByte()
        for (size: Int in mFrameSize) {
            data[offset++] = ((size shr 24) and 0xFF).toByte()
            data[offset++] = ((size shr 16) and 0xFF).toByte()
            data[offset++] = ((size shr 8) and 0xFF).toByte()
            data[offset++] = (size and 0xFF).toByte()
        }
        atom.setData(data)
        return atom
    }

    private fun getSTCOAtom(): Atom {
        val atom: Atom = Atom("stco", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            0, 0, 0, 0x01,  // entry count
            0, 0, 0, 0 // chunk offset. Set to 0 here. Must be set later. Here it should be
            // the size of the complete header, as the AAC stream will follow
            // immediately.
        ))
        return atom
    }

    companion object {
        fun getMP4Header(
            sampleRate: Int, numChannels: Int, frame_size: IntArray?, bitrate: Int
        ): ByteArray? {
            return MP4Header(sampleRate, numChannels, frame_size, bitrate).mHeader
        }
    }

    // Creates a new MP4Header object that should be used to generate an .m4a file header.
    init {
        mSampleRate = sampleRate
        mChannels = numChannels
        mFrameSize = frame_size
        mBitrate = bitrate
        mMaxFrameSize = mFrameSize!![0]
        mTotSize = mFrameSize[0]
        for (i in 1 until mFrameSize.size) {
            if (mMaxFrameSize < mFrameSize[i]) {
                mMaxFrameSize = mFrameSize[i]
            }
            mTotSize += mFrameSize[i]
        }
        var time: Long = System.currentTimeMillis() / 1000
        time += ((66 * 365 + 16) * 24 * 60 * 60).toLong() // number of seconds between 1904 and 1970
        mTime = ByteArray(4)
        mTime[0] = ((time shr 24) and 0xFF).toByte()
        mTime[1] = ((time shr 16) and 0xFF).toByte()
        mTime[2] = ((time shr 8) and 0xFF).toByte()
        mTime[3] = (time and 0xFF).toByte()
        val numSamples: Int = 1024 * (frame_size!!.size - 1) // 1st frame does not contain samples.
        var durationMS: Int = (numSamples * 1000) / mSampleRate
        if ((numSamples * 1000) % mSampleRate > 0) {  // round the duration up.
            durationMS++
        }
        mNumSamples = byteArrayOf(
            ((numSamples shr 26) and 0XFF).toByte(),
            ((numSamples shr 16) and 0XFF).toByte(),
            ((numSamples shr 8) and 0XFF).toByte(),
            (numSamples and 0XFF).toByte()
        )
        mDurationMS = byteArrayOf(
            ((durationMS shr 26) and 0XFF).toByte(),
            ((durationMS shr 16) and 0XFF).toByte(),
            ((durationMS shr 8) and 0XFF).toByte(),
            (durationMS and 0XFF).toByte()
        )
        setHeader()
    }
}