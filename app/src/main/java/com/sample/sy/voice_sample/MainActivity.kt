package com.sample.sy.voice_sample

import android.Manifest
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.ERROR
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap

/**
 * 출처 : http://jeongchul.tistory.com/339
 */
class MainActivity : AppCompatActivity() {

    // speech 콜백 Request code
    private val RESULT_SPEECH = 1000

    // 권한 요청 구분 코드
    private val REQUEST_MICROPHONE = 11

    private lateinit var mTts : TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_voice.setOnClickListener {
            if (isCheckPermission()) speekRecordition()
            else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
        }

        mTts = TextToSpeech(this, TextToSpeech.OnInitListener { s -> if (ERROR == s) mTts.language = Locale.KOREA })
    }

    /**
     * 권한 사용유무 확인
     */
    private fun isCheckPermission() : Boolean {
        val permission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)

        val isUsed = true
        when (permission) {
            PackageManager.PERMISSION_DENIED -> return false
        }
        return isUsed
    }

    /**
     * 음성인식 함수
     */
    private fun speekRecordition() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName) // 호출한 패키지 이름
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-kr")      // 인식할 언어 선택
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "말해주세요")      // 유저에게 보여줄 문자

        try {
            startActivityForResult(i, RESULT_SPEECH)
        } catch (e : ActivityNotFoundException) {
            Toast.makeText(applicationContext, "Speech to Text를 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
            e.stackTrace
        }
    }

    /**
     *  음성 출력
     */
    private fun speekMessage(msg : String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ttsGreater21(msg)
        else ttsUnder20(msg)
    }

    @SuppressWarnings("deprecation")
    private fun ttsUnder20(msg : String) {
        val params = HashMap<String, String>()

        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, msg)
        mTts.speak(msg, TextToSpeech.QUEUE_ADD, params)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ttsGreater21(msg : String) {
        val utteranceId = this.hashCode().toString() + msg

        mTts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, msg)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_MICROPHONE -> {
                var i = 0
                permissions.forEach {
                    p ->
                    val grantResult = grantResults[i]
                    if (Manifest.permission.RECORD_AUDIO == p) {
                        if (PackageManager.PERMISSION_GRANTED == grantResult) {
                            Toast.makeText(applicationContext, "레코드 권한을 허용하셨습니다", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "레코드 권한을 허용하지 않으면 정상동작하지 않습니다.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_SPEECH -> if (RESULT_OK == resultCode) {
                val resultArray = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                // 가장 유사한 단어부터 시작되는 배열중에서 0번째를 꺼냄
                val result = resultArray[0]
                msg_view.text = result.toString()
                speekMessage(result)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
