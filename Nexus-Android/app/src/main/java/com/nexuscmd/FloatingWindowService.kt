package com.nexuscmd

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

class FloatingWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var isExpanded = false
    private var originalX = 0
    private var originalY = 0
    private var touchX = 0f
    private var touchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        showFloatingWindow()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingWindow()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MC命令助手悬浮窗",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "悬浮窗服务通知"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MC命令助手")
            .setContentText("悬浮窗正在运行")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showFloatingWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_window, null)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 200

        setupFloatingView(floatingView!!, params)

        windowManager?.addView(floatingView, params)
    }

    private fun setupFloatingView(view: View, params: WindowManager.LayoutParams) {
        val collapsedView = view.findViewById<LinearLayout>(R.id.collapsed_view)
        val expandedView = view.findViewById<LinearLayout>(R.id.expanded_view)
        val commandInput = view.findViewById<EditText>(R.id.command_input)
        val closeBtn = view.findViewById<ImageButton>(R.id.btn_close)
        val minimizeBtn = view.findViewById<ImageButton>(R.id.btn_minimize)
        val completionsContainer = view.findViewById<LinearLayout>(R.id.completions_container)
        val statusText = view.findViewById<TextView>(R.id.status_text)

        val helper = CommandHelper.Registry.getInstance()

        // Toggle expand/collapse on collapsed view click
        collapsedView.setOnClickListener {
            if (!isExpanded) {
                isExpanded = true
                collapsedView.visibility = View.GONE
                expandedView.visibility = View.VISIBLE

                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                params.width = (resources.displayMetrics.widthPixels * 0.85f).toInt()
                windowManager?.updateViewLayout(view, params)

                commandInput.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(commandInput, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        minimizeBtn.setOnClickListener {
            collapseWindow(view, params, collapsedView, expandedView)
        }

        closeBtn.setOnClickListener {
            stopSelf()
        }

        // Command input text change
        commandInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s?.toString() ?: ""
                val cursorPos = commandInput.selectionEnd

                // Update completions
                val completionsJson = helper.getCompletions(text, cursorPos)
                val completions = parseCompletions(completionsJson)

                completionsContainer.removeAllViews()
                completions.take(4).forEach { item ->
                    val chip = layoutInflater.inflate(R.layout.completion_chip, null)
                    val chipText = chip.findViewById<TextView>(R.id.chip_text)
                    val chipDetail = chip.findViewById<TextView>(R.id.chip_detail)
                    chipText.text = item.label
                    if (item.detail.isNotEmpty()) {
                        chipDetail.text = item.detail
                        chipDetail.visibility = View.VISIBLE
                    } else {
                        chipDetail.visibility = View.GONE
                    }
                    chip.setOnClickListener {
                        commandInput.setText(item.insertText)
                        commandInput.setSelection(item.insertText.length)
                    }
                    completionsContainer.addView(chip)
                }

                // Update validation
                val validationJson = helper.validateCommand(text)
                val validation = parseValidation(validationJson)
                when {
                    text.isEmpty() -> {
                        statusText.text = "输入命令..."
                        statusText.setTextColor(android.graphics.Color.GRAY)
                    }
                    validation.hasError -> {
                        statusText.text = "✗ ${validation.message ?: "语法错误"}"
                        statusText.setTextColor(0xFFE06C75.toInt())
                    }
                    else -> {
                        statusText.text = "✓ 命令正确"
                        statusText.setTextColor(0xFF4CAF50.toInt())
                    }
                }
            }
        })

        // Drag functionality
        var initialX = 0
        var initialY = 0
        var startX = 0f
        var startY = 0f

        collapsedView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    startX = event.rawX
                    startY = event.rawY
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startX
                    val dy = event.rawY - startY
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        windowManager?.updateViewLayout(view, params)
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun collapseWindow(
        view: View,
        params: WindowManager.LayoutParams,
        collapsedView: LinearLayout,
        expandedView: LinearLayout
    ) {
        isExpanded = false
        collapsedView.visibility = View.VISIBLE
        expandedView.visibility = View.GONE

        params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        windowManager?.updateViewLayout(view, params)

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun removeFloatingWindow() {
        floatingView?.let {
            windowManager?.removeView(it)
            floatingView = null
        }
    }

    private fun parseCompletions(json: String): List<CompletionItem> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                CompletionItem(
                    label = obj.getString("label"),
                    detail = obj.optString("detail", ""),
                    insertText = obj.getString("insertText")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseValidation(json: String): ValidationResult {
        return try {
            val obj = JSONObject(json)
            ValidationResult(
                hasError = obj.getBoolean("hasError"),
                message = obj.optString("message", null),
                position = if (obj.has("position")) obj.getInt("position") else null
            )
        } catch (e: Exception) {
            ValidationResult(hasError = true, message = e.message)
        }
    }

    companion object {
        private const val CHANNEL_ID = "floating_window_channel"
        private const val NOTIFICATION_ID = 1
    }
}
