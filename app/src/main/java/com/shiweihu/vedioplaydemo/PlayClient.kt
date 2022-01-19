package com.shiweihu.vedioplaydemo

import android.animation.TimeAnimator
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Surface
import java.lang.Exception
import java.util.*

class PlayClient(private val surface:Surface) {

    private val mExtractor = MediaExtractor()

    private var videoDecoder: MediaCodec? = null
    private var audioDecoder: MediaCodec? = null
    private val handlerThread = HandlerThread("Decode Video Thread")
    private val handler by lazy {
        handlerThread.start()
        Handler(handlerThread.looper)
    }

    private val mTimeAnimator = TimeAnimator()

    private val queue =  LinkedList<Map<Int,MediaCodec.BufferInfo>>()

    private var startTime = 0L


    fun startPlay(context: Context, path:String){
        try{
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
                                 Log.println(Log.DEBUG,"decode","${System.currentTimeMillis() - startTime}")
                                 if (index != MediaCodec.INFO_TRY_AGAIN_LATER){
                                        val inputBuffer = codec.getInputBuffer(index)
                                        val size = mExtractor.readSampleData(inputBuffer!!,0)
                                        var flags = mExtractor.sampleFlags
                                        if (size <= 0) {
                                            flags = (flags or MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                        }
                                        codec.queueInputBuffer(index, 0,
                                            size.coerceAtLeast(0), mExtractor.sampleTime, flags)
                                     Log.println(Log.DEBUG,"sampleTime","${mExtractor.sampleTime}")
                                     mExtractor.advance()
                                     val current = System.currentTimeMillis()
//                                     if((current - startTime)*1000 >=mExtractor.sampleTime){
//                                         Log.println(Log.DEBUG,"advance","${(current - startTime)*1000} - ${mExtractor.sampleTime}")
//                                         mExtractor.advance()
//                                     }
                                        //
                                 }

                            }

                            override fun onOutputBufferAvailable(
                                codec: MediaCodec,
                                index: Int,
                                info: MediaCodec.BufferInfo
                            ) {
                                if (index != MediaCodec.INFO_TRY_AGAIN_LATER){
                                    val outputBuffer = codec.getOutputBuffer(index)
                                    Thread.sleep(((info.presentationTimeUs - startTime)/ 1000).coerceAtLeast(0) )
                                    startTime = info.presentationTimeUs
                                    codec.releaseOutputBuffer(index,true)
                                    val isEos = mExtractor.sampleFlags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                    if(info.size <= 0 && isEos){
                                        codec.stop()
                                        codec.release()
                                        mExtractor.release()
                                        startTime = 0L

                                    }
                                }
                            }

                            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {

                            }

                            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {

                            }

                        },handler)
                    }
                    videoDecoder?.configure(trackFormat, surface, null, 0)
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
            //startTime = System.currentTimeMillis()
            videoDecoder?.start()



        }catch (e:Exception){
            Log.println(Log.VERBOSE,"exception",e.message!!)
        }

    }
}