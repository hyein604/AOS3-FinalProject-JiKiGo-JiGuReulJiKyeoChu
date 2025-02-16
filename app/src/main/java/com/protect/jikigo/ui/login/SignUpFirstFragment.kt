package com.protect.jikigo.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import com.protect.jikigo.R
import com.protect.jikigo.data.WebSiteURL
import com.protect.jikigo.databinding.FragmentSignUpFirstBinding
import com.protect.jikigo.ui.extensions.applySpannableStyles


class SignUpFirstFragment : Fragment() {
    private var _binding: FragmentSignUpFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClick()
        onClickMore()
        spannableText()
        setCheckBox()
    }

    // 클릭 메서드
    private fun onClick() {
        binding.btnSignUpConfirm.setOnClickListener {
            val action = SignUpFirstFragmentDirections.actionSignUpFirstToSignUpSecond()
            findNavController().navigate(action)
        }
        binding.toolbarSignUp.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun onClickMore() {
        binding.tvSignUpServiceMore.setOnClickListener {
            val action = SignUpFirstFragmentDirections.actionSignUpFirstToWebView(WebSiteURL.APP_SERVICE)
            findNavController().navigate(action)
        }
        binding.tvSignUpPersonal.setOnClickListener {
            val action = SignUpFirstFragmentDirections.actionSignUpFirstToWebView(WebSiteURL.APP_PERSONAL)
            findNavController().navigate(action)
        }
    }

    private fun spannableText() {
        binding.tvSignUpServiceMore.applySpannableStyles(0, binding.tvSignUpServiceMore.length(), R.color.gray_50, true, true)
        binding.tvSignUpPersonal.applySpannableStyles(76, 84, R.color.gray_50, true, true)
    }


    private fun setCheckBox() {
        with(binding) {
            val cbList = listOf(cbSignUpAge, cbSignUpService)

            cbSignUpAll.setOnCheckedChangeListener { _, isChecked ->
                val state = if (isChecked) MaterialCheckBox.STATE_CHECKED else MaterialCheckBox.STATE_UNCHECKED
                setChildCheckBoxes(state)
                updateButtonState(cbList)
            }

            cbList.forEach {
                it.setOnCheckedChangeListener { _, _ ->
                    updateParentCheckBoxState(cbList)
                    updateButtonState(cbList)
                }
            }
        }
    }

    private fun setChildCheckBoxes(state: Int) {
        with(binding) {
            cbSignUpAge.setOnCheckedChangeListener(null)
            cbSignUpService.setOnCheckedChangeListener(null)

            cbSignUpAge.checkedState = state
            cbSignUpService.checkedState = state

            cbSignUpAge.setOnCheckedChangeListener { _, _ ->
                updateParentCheckBoxState(listOf(cbSignUpAge, cbSignUpService))
                updateButtonState(listOf(cbSignUpAge, cbSignUpService))

            }
            cbSignUpService.setOnCheckedChangeListener { _, _ ->
                updateParentCheckBoxState(listOf(cbSignUpAge, cbSignUpService))
                updateButtonState(listOf(cbSignUpAge, cbSignUpService))
            }
        }
    }

    private fun updateParentCheckBoxState(cbList: List<MaterialCheckBox>) {
        with(binding) {
            val checkedCount = cbList.count { it.isChecked }

            cbSignUpAll.setOnCheckedChangeListener(null)
            cbSignUpAll.checkedState = when (checkedCount) {
                cbList.size -> MaterialCheckBox.STATE_CHECKED
                0 -> MaterialCheckBox.STATE_UNCHECKED
                else -> MaterialCheckBox.STATE_INDETERMINATE
            }
            cbSignUpAll.setOnCheckedChangeListener { _, isChecked ->
                val state = if (isChecked) MaterialCheckBox.STATE_CHECKED else MaterialCheckBox.STATE_UNCHECKED
                setChildCheckBoxes(state)
                updateButtonState(cbList)
            }
        }
    }

    private fun updateButtonState(cbList: List<MaterialCheckBox>) {
        with(binding) {
            btnSignUpConfirm.isEnabled = cbList.all { it.isChecked }
        }
    }
}