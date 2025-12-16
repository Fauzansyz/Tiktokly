package com.zandeveloper.tiktokly.utils.userHelp

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import com.zandeveloper.tiktokly.R
import com.zandeveloper.tiktokly.databinding.ActivityMainBinding
import com.zandeveloper.tiktokly.databinding.SpotlightOverlayBinding

class UserHelpApp private constructor(
    private val activity: Activity,
    private val binding: ActivityMainBinding
) {

    private lateinit var spotlight: Spotlight
    private lateinit var targets: List<Target>

    fun startHelp() {

        val target1 = Target.Builder()
            .setAnchor(binding.inputTextContainer)
            .setShape(
                RoundedRectangle(
                    width = binding.inputTextContainer.width + 40f,
                    height = binding.inputTextContainer.height + 40f,
                    radius = 20f
                )
            )
            .setOverlay(createOverlay(activity.getString(R.string.tutorial_title_first), 
             activity.getString(R.string.tutorial_msg_first)))
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {}
                override fun onEnded() {}
            })
            .build()

        val target2 = Target.Builder()
            .setAnchor(binding.buttonDownload)
            .setShape(
                RoundedRectangle(
                    width = binding.buttonDownload.width + 40f,
                    height = binding.buttonDownload.height + 40f,
                    radius = 20f
                )
            )
            .setOverlay(createOverlay(activity.getString(R.string.tutorial_title_second),
             activity.getString(R.string.tutorial_msg_second)))
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {}
                override fun onEnded() {
                    spotlight.finish()
                }
            })
            .build()

        targets = listOf(target1, target2)

        spotlight = Spotlight.Builder(activity)
            .setTargets(targets)
            .setBackgroundColorRes(R.color.black_80)
            .setDuration(300L)
            .setAnimation(DecelerateInterpolator(2f))
            .build()

        spotlight.start()
    }

    private fun createOverlay(title: String, sub: String): View {
        val overlayBinding = SpotlightOverlayBinding.inflate(
            LayoutInflater.from(activity)
        )

        overlayBinding.tvTitle.text = title
        overlayBinding.tvSubText.text = sub

        overlayBinding.root.setOnClickListener {
            spotlight.next()
        }

        return overlayBinding.root
    }

    // 🔥🔥🔥 BUILDER TERPISAH DAN CLEAN 🔥🔥🔥
    class Builder {
        private var activity: Activity? = null
        private var binding: ActivityMainBinding? = null

        fun setActivity(activity: Activity) = apply {
            this.activity = activity
        }

        fun setBinding(binding: ActivityMainBinding) = apply {
            this.binding = binding
        }

        fun build(): UserHelpApp {
            val act = activity
                ?: throw IllegalStateException("Activity belum di-set di UserHelp.Builder")

            val bind = binding
                ?: throw IllegalStateException("Binding belum di-set di UserHelp.Builder")

            return UserHelpApp(act, bind)
        }

        fun startHelp() {
            build().startHelp()
        }
    }
}