package com.shiweihu.vedioplaydemo

import android.animation.TimeAnimator
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Surface

class PlayClient(private val surface:Surface) {

    private val mExtractor = MediaExtractor()

    private var videoDecoder: MediaCodec? = null
    private var audioDecoder: MediaCodec? = null

    private val handler = Handler(Looper.getMainLooper())

    private val mTimeAnimator = TimeAnimator()

    fun startPlay(context: Context, path:String){
        mExtractor.setDataSource(path)

        for (i in 0 until mExtractor.trackCount){
            mExtractor.unselectTrack(i)
            val trackFormat = mExtractor.getTrackFormat(i)
            val mimeType = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""

            if(mimeType.contains("video/")){
                //视频流
                videoDecoder = MediaCodec.createDecoderByType(mimeType)
                videoDecoder?.let { codec ->
                    codec.setCallback(object:MediaCodec.Callback(){
                        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
//                            val isEos =
//                                mExtractor.sampleFlags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            if (index != MediaCodec.INFO_TRY_AGAIN_LATER){
                                    val inputBuffer = codec.getInputBuffer(index)
                                    val size = mExtractor.readSampleData(inputBuffer!!,0)
                                    var flags = mExtractor.sampleFlags
                                    if (size <= 0) {
                                        flags = (flags or MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                    }

                                    codec.queueInputBuffer(index, 0,
                                        size.coerceAtLeast(0), mExtractor.sampleTime, flags)
                                    mExtractor.advance()
                            }

                        }

                        override fun onOutputBufferAvailable(
                            codec: MediaCodec,
                            index: Int,
                            info: MediaCodec.BufferInfo
                        ) {
                            val outputBuffer = codec.getOutputBuffer(index)
                            codec.releaseOutputBuffer(index,true)
                            val isEos = mExtractor.sampleFlags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            if(info.size <= 0 && isEos){
                                codec.stop()
                                codec.release()
                                mExtractor.release()
                            }


                        }

                        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {

                        }

                        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {

                        }

                    },handler)
                }
                videoDecoder!!.configure(trackFormat, surface, null, 0)
                mExtractor.selectTrack(i)
                continue
            }
            if(mimeType.contains("audio/")){
                //音频流
//                audioDecoder = MediaCodec.createDecoderByType(mimeType)
//                audioDecoder!!.configure(trackFormat, null, null, 0)
//                mExtractor.selectTrack(i)
                continue
            }
        }
        videoDecoder?.start()
    }
}