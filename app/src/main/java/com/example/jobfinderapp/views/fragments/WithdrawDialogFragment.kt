package com.example.jobfinderapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.jobfinderapp.databinding.WithdrawDialogLayoutBinding
import com.google.android.material.transition.MaterialElevationScale


class WithdrawDialogFragment : DialogFragment() {

    init {
        enterTransition = MaterialElevationScale(true).apply {
          duration = 320
        }

        returnTransition = MaterialElevationScale(false).apply {
            duration = 150
        }
    }

    private var _binding: WithdrawDialogLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        //Это мы делаем, для того, чтобы убрать системную подставку(фон), точнее делаем ее прозрачной, чтобы
        //наш закругленный фон, был виден.
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        _binding = WithdrawDialogLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animateImageView()
        animateBtn(binding.withdrawConfirmBtn)
        animateBtn(binding.withdrawDisagreeBtn)

        binding.withdrawConfirmBtn.setOnClickListener {
            //Отправляем слушателю с таким же RequestKey, bundle, который является результатом
            setFragmentResult(WITHDRAW_REQUEST_KEY, bundleOf(WITHDRAW_BUNDLE_KEY to true))
            dismiss()
        }

        binding.withdrawDisagreeBtn.setOnClickListener {
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()

        //Тут расширяем наш диалог, так как он по умолчанию сжат системой.
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(), // 95% ширины экрана
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun animateBtn(view: View) {

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.isPressed = true

                    v.animate()
                        .scaleX(0.92f)
                        .scaleY(0.92f)
                        .setDuration(100)
                        .start()

                    true
                }

                MotionEvent.ACTION_UP -> {
                    v.isPressed = false

                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    v.performClick()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false

                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    true
                }

                else -> false
            }

        }
    }

    private fun animateImageView() {
        binding.withdrawCancelIv.apply {
            alpha = 0f
            scaleX = 0.3f
            scaleY = 0.3f
        }

        binding.withdrawCancelIv.animate()
            .alpha(1f)
            .setDuration(150)
            .start()

        SpringAnimation(binding.withdrawCancelIv, SpringAnimation.SCALE_X, 1f).apply {
            spring = SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            }
            start()
        }

        SpringAnimation(binding.withdrawCancelIv, SpringAnimation.SCALE_Y, 1f).apply {
            spring = SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            }
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        const val WITHDRAW_REQUEST_KEY = "WithdrawRequest"
        const val WITHDRAW_BUNDLE_KEY = "confirm"
    }
}