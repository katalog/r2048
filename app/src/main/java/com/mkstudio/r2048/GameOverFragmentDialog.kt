package com.mkstudio.r2048

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mkstudio.r2048.databinding.GameoverdialogBinding

class GameOverFragmentDialog(val result:Int) : DialogFragment() {
    private lateinit var binding : GameoverdialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GameoverdialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvScoreResult.text = result.toString()

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }
}