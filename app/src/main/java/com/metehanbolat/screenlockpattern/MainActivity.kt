package com.metehanbolat.screenlockpattern

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.metehanbolat.screenlockpattern.component.PatternViewStageState
import com.metehanbolat.screenlockpattern.component.PatternViewState
import com.metehanbolat.screenlockpattern.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        clearTextButtonClickListener()
        stageButtonClickListener()
        patternLockViewChangeStateListener()
        patternViewState()

    }

    private fun clearTextButtonClickListener() {
        binding.clearTextButton.setOnClickListener {
            viewModel.updateViewState(PatternViewState.Initial)
        }
    }

    private fun stageButtonClickListener() {
        binding.apply {
            stageButton.setOnClickListener {
                when(patternLockView.stageState) {
                    PatternViewStageState.FIRST -> {
                        viewModel.updateViewState(PatternViewState.Initial)
                        patternLockView.stageState = PatternViewStageState.SECOND
                        stageButton.text = resources.getString(R.string.stage_button_confirm)
                        tvSubTitle.isInvisible = true
                    }
                    PatternViewStageState.SECOND -> {
                        AlertDialog.Builder(this@MainActivity).apply {
                            setMessage(R.string.alert_dialog_confirm_message)
                            setPositiveButton(R.string.alert_dialog_positive_button) { _, _ -> }
                        }.show()
                    }
                }
            }
        }
    }

    private fun patternLockViewChangeStateListener() {
        binding.patternLockView.setOnChangeStateListener { state ->
            viewModel.updateViewState(state)
        }
    }

    private fun patternViewState() {
        lifecycleScope.launchWhenCreated {
            viewModel.viewState.collect { patternViewState ->
                binding.apply {
                    when(patternViewState) {
                        is PatternViewState.Initial -> {
                            patternLockView.reset()
                            stageButton.isEnabled = false
                            clearTextButton.isVisible = false
                            tvMessage.run {
                                text = if (patternLockView.stageState == PatternViewStageState.FIRST) {
                                    resources.getString(R.string.initial_message_first_stage)
                                } else {
                                    resources.getString(R.string.initial_message_second_stage)
                                }
                                setTextColor(ContextCompat.getColor(context, R.color.message_text_default_color))
                            }
                        }
                        is PatternViewState.Started -> {
                            tvMessage.run {
                                text = resources.getString(R.string.started_message)
                                setTextColor(ContextCompat.getColor(context, R.color.message_text_default_color))
                            }
                        }
                        is PatternViewState.Success -> {
                            stageButton.isEnabled = true
                            tvMessage.run {
                                text = if (patternLockView.stageState == PatternViewStageState.FIRST) {
                                    resources.getString(R.string.success_message_first_stage)
                                } else {
                                    resources.getString(R.string.success_message_second_stage)
                                }
                                setTextColor(ContextCompat.getColor(context, R.color.message_text_default_color))
                            }
                            clearTextButton.isVisible = patternLockView.stageState == PatternViewStageState.FIRST
                        }
                        is PatternViewState.Error -> {
                            tvMessage.run {
                                text = if (patternLockView.stageState == PatternViewStageState.FIRST) {
                                    resources.getString(R.string.error_message_first_stage)
                                } else {
                                    resources.getString(R.string.error_message_second_stage)
                                }
                                setTextColor(ContextCompat.getColor(context, R.color.message_text_error_color))
                            }
                            clearTextButton.isVisible = patternLockView.stageState == PatternViewStageState.FIRST
                        }
                    }
                }
            }
        }
    }
}