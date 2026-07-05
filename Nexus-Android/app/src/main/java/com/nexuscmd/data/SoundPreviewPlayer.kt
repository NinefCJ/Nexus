package com.nexuscmd.data

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SoundPreviewPlayer {
    private var toneGenerator: ToneGenerator? = null
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentPlaybackRunnable: Runnable? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSoundId = MutableStateFlow<String?>(null)
    val currentSoundId: StateFlow<String?> = _currentSoundId.asStateFlow()

    fun playTonePreview(effect: SoundEffect) {
        stop()

        val tone = if (effect.previewTone >= 0) {
            effect.previewTone
        } else {
            getDefaultToneForCategory(effect.category)
        }

        val duration = if (effect.previewDuration > 0) effect.previewDuration else 350

        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            }
            toneGenerator?.startTone(tone, duration)
            _isPlaying.value = true
            _currentSoundId.value = effect.id

            currentPlaybackRunnable = Runnable {
                _isPlaying.value = false
                _currentSoundId.value = null
            }
            handler.postDelayed(currentPlaybackRunnable!!, duration.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playMultiTonePreview(effect: SoundEffect) {
        stop()

        val tones = getToneSequenceForEffect(effect)
        if (tones.isEmpty()) {
            playTonePreview(effect)
            return
        }

        _isPlaying.value = true
        _currentSoundId.value = effect.id

        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            }

            var delay = 0L
            tones.forEach { (tone, duration) ->
                handler.postDelayed({
                    toneGenerator?.startTone(tone, duration)
                }, delay)
                delay += duration
            }

            currentPlaybackRunnable = Runnable {
                _isPlaying.value = false
                _currentSoundId.value = null
            }
            handler.postDelayed(currentPlaybackRunnable!!, delay)
        } catch (e: Exception) {
            e.printStackTrace()
            playTonePreview(effect)
        }
    }

    private fun getToneSequenceForEffect(effect: SoundEffect): List<Pair<Int, Int>> {
        return when {
            effect.id.contains("explode") || effect.id.contains("blast") -> {
                listOf(
                    ToneGenerator.TONE_DTMF_0 to 100,
                    ToneGenerator.TONE_DTMF_1 to 80,
                    ToneGenerator.TONE_DTMF_2 to 120,
                    ToneGenerator.TONE_DTMF_3 to 150
                )
            }
            effect.id.contains("break") || effect.id.contains("dig") -> {
                listOf(
                    ToneGenerator.TONE_DTMF_5 to 60,
                    ToneGenerator.TONE_DTMF_6 to 80,
                    ToneGenerator.TONE_DTMF_4 to 100
                )
            }
            effect.id.contains("step") || effect.id.contains("walk") -> {
                listOf(
                    ToneGenerator.TONE_DTMF_7 to 50,
                    ToneGenerator.TONE_DTMF_9 to 50,
                    ToneGenerator.TONE_DTMF_7 to 50
                )
            }
            effect.id.contains("hurt") || effect.id.contains("damage") -> {
                listOf(
                    ToneGenerator.TONE_PROP_NACK to 150,
                    ToneGenerator.TONE_DTMF_0 to 100
                )
            }
            effect.id.contains("death") -> {
                listOf(
                    ToneGenerator.TONE_DTMF_3 to 200,
                    ToneGenerator.TONE_DTMF_2 to 200,
                    ToneGenerator.TONE_DTMF_1 to 300
                )
            }
            effect.id.contains("attack") -> {
                listOf(
                    ToneGenerator.TONE_DTMF_9 to 40,
                    ToneGenerator.TONE_DTMF_8 to 60,
                    ToneGenerator.TONE_DTMF_7 to 80
                )
            }
            effect.id.contains("firework") || effect.id.contains("launch") -> {
                listOf(
                    ToneGenerator.TONE_DTMF_1 to 50,
                    ToneGenerator.TONE_DTMF_3 to 50,
                    ToneGenerator.TONE_DTMF_5 to 50,
                    ToneGenerator.TONE_DTMF_7 to 100
                )
            }
            effect.id.contains("portal") -> {
                listOf(
                    ToneGenerator.TONE_DTMF_A to 100,
                    ToneGenerator.TONE_DTMF_B to 100,
                    ToneGenerator.TONE_DTMF_C to 100,
                    ToneGenerator.TONE_DTMF_D to 150
                )
            }
            effect.id.contains("note") || effect.id.contains("music") -> {
                listOf(
                    ToneGenerator.TONE_DTMF_1 to 100,
                    ToneGenerator.TONE_DTMF_3 to 100,
                    ToneGenerator.TONE_DTMF_5 to 100,
                    ToneGenerator.TONE_DTMF_8 to 200
                )
            }
            else -> emptyList()
        }
    }

    private fun getDefaultToneForCategory(category: String): Int {
        return when (category) {
            "环境" -> ToneGenerator.TONE_DTMF_6
            "天气" -> ToneGenerator.TONE_DTMF_0
            "装备" -> ToneGenerator.TONE_PROP_ACK
            "信标" -> ToneGenerator.TONE_DTMF_A
            "方块" -> ToneGenerator.TONE_PROP_BEEP
            "物品" -> ToneGenerator.TONE_PROP_PROMPT
            "气泡" -> ToneGenerator.TONE_DTMF_7
            "桶" -> ToneGenerator.TONE_DTMF_5
            "炼药锅" -> ToneGenerator.TONE_DTMF_4
            "铜" -> ToneGenerator.TONE_DTMF_8
            "武器" -> ToneGenerator.TONE_DTMF_9
            "伤害" -> ToneGenerator.TONE_PROP_NACK
            "挖掘" -> ToneGenerator.TONE_DTMF_5
            "滴水" -> ToneGenerator.TONE_DTMF_7
            "鞘翅" -> ToneGenerator.TONE_DTMF_3
            "实体" -> ToneGenerator.TONE_DTMF_2
            "坠落" -> ToneGenerator.TONE_DTMF_1
            "火焰" -> ToneGenerator.TONE_DTMF_0
            "烟花" -> ToneGenerator.TONE_DTMF_9
            "玩家" -> ToneGenerator.TONE_PROP_ACK
            "击打" -> ToneGenerator.TONE_DTMF_6
            "跳跃" -> ToneGenerator.TONE_DTMF_8
            "着陆" -> ToneGenerator.TONE_DTMF_4
            "栓绳" -> ToneGenerator.TONE_DTMF_5
            "液体" -> ToneGenerator.TONE_DTMF_7
            "磁石" -> ToneGenerator.TONE_DTMF_A
            "矿车" -> ToneGenerator.TONE_DTMF_3
            "生物" -> ToneGenerator.TONE_DTMF_2
            "音乐" -> ToneGenerator.TONE_DTMF_8
            "音符盒" -> ToneGenerator.TONE_DTMF_8
            "粒子" -> ToneGenerator.TONE_DTMF_6
            "采集" -> ToneGenerator.TONE_DTMF_5
            "放置" -> ToneGenerator.TONE_PROP_BEEP
            "传送门" -> ToneGenerator.TONE_DTMF_A
            "劫掠" -> ToneGenerator.TONE_DTMF_0
            "随机" -> ToneGenerator.TONE_PROP_BEEP
            "唱片" -> ToneGenerator.TONE_DTMF_8
            "重生锚" -> ToneGenerator.TONE_DTMF_3
            "锻造台" -> ToneGenerator.TONE_DTMF_5
            "脚步" -> ToneGenerator.TONE_DTMF_4
            "红石" -> ToneGenerator.TONE_DTMF_5
            "界面" -> ToneGenerator.TONE_PROP_PROMPT
            "使用" -> ToneGenerator.TONE_PROP_BEEP
            "潮涌核心" -> ToneGenerator.TONE_DTMF_A
            "教育版" -> ToneGenerator.TONE_PROP_ACK
            else -> ToneGenerator.TONE_PROP_BEEP
        }
    }

    fun stop() {
        currentPlaybackRunnable?.let {
            handler.removeCallbacks(it)
            currentPlaybackRunnable = null
        }
        try {
            toneGenerator?.stopTone()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isPlaying.value = false
        _currentSoundId.value = null
    }

    fun release() {
        stop()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
