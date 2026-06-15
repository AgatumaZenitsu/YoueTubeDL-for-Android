package com.example.youtubeaudioextractor
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // VIPパス（全ファイルアクセスの許可）を要求するコード
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:" + packageName)
                startActivity(intent)
            }
        }

        // 画面のパーツを変数にまとめる
        val urlInput = findViewById<EditText>(R.id.urlInput)
        val downloadAudioButton = findViewById<Button>(R.id.downloadAudioButton) // MP3ボタン
        val downloadVideoButton = findViewById<Button>(R.id.downloadVideoButton) // MP4ボタン
        val statusText = findViewById<TextView>(R.id.statusText)

        downloadAudioButton.isEnabled = false
        downloadVideoButton.isEnabled = false

        // エンジンの初期化処理
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    statusText.text = "エンジンを初期化・更新しています...\n（※最新版をダウンロードするため、少し時間がかかります）"
                }
                YoutubeDL.getInstance().init(application)
                FFmpeg.getInstance().init(application)
                YoutubeDL.getInstance().updateYoutubeDL(application)
                withContext(Dispatchers.Main) {
                    statusText.text = "準備完了：URLを入れてください"
                    downloadAudioButton.isEnabled = true
                    downloadVideoButton.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "初期化エラー: ${e.message}"
                }
            }
        }

        // 可愛いダンス動画を無限ループ再生するコード
        val danceVideoView = findViewById<android.widget.VideoView>(R.id.danceVideoView)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.dance_anime
        danceVideoView.setVideoURI(android.net.Uri.parse(videoPath))
        danceVideoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            danceVideoView.start()
        }

        // ダウンロードの実行をまとめた関数
        fun startDownload(videoUrl: String, isVideo: Boolean) {
            statusText.text = "ダウンロードと抽出を開始しています...\n（数分かかる場合があります）"
            downloadAudioButton.isEnabled = false
            downloadVideoButton.isEnabled = false

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = YoutubeDLRequest(videoUrl)

                    if (isVideo) {
                        // 動画 (MP4) として保存する設定
                        request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best")
                    } else {
                        // 音源 (MP3) として保存する設定
                        request.addOption("-x")
                        request.addOption("--audio-format", "mp3")
                    }

                    // YouTubeブロック回避オプション
                    request.addOption("--extractor-args", "youtube:player_client=android")

                    // スマホのダウンロードフォルダに保存する
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    request.addOption("-o", "${downloadsDir.absolutePath}/%(title)s.%(ext)s")

                    YoutubeDL.getInstance().execute(request, "Task1", null)

                    withContext(Dispatchers.Main) {
                        statusText.text = "完了しました！\nスマホの「ダウンロード」フォルダを確認してください。"
                        downloadAudioButton.isEnabled = true
                        downloadVideoButton.isEnabled = true
                        urlInput.text.clear()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        statusText.text = "エラーが発生しました:\n${e.message}"
                        downloadAudioButton.isEnabled = true
                        downloadVideoButton.isEnabled = true
                    }
                }
            }
        }

        // 音源(MP3)ボタンが押されたときの処理
        downloadAudioButton.setOnClickListener {
            val videoUrl = urlInput.text.toString()
            if (videoUrl.isEmpty()) {
                Toast.makeText(this, "URLを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startDownload(videoUrl, false) // false = 音声として保存
        }

        // 動画(MP4)ボタンが押されたときの処理
        downloadVideoButton.setOnClickListener {
            val videoUrl = urlInput.text.toString()
            if (videoUrl.isEmpty()) {
                Toast.makeText(this, "URLを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startDownload(videoUrl, true) // true = 動画として保存
        }
    }
}