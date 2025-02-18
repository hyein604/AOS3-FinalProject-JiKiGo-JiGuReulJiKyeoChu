package com.protect.jikigo.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.protect.jikigo.R
import com.protect.jikigo.databinding.FragmentFindIdBinding
import com.protect.jikigo.ui.extensions.setTimer


class FindIdFragment : Fragment() {
    private var _binding: FragmentFindIdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        onClickAuthBtn()
        editTextWatcher()
        onClickAuthCheckBtn()
        onClickFindIdBtn()
    }

    private fun editTextWatcher() {
        val regexName = "^[가-힣]{2,8}$".toRegex()
        val regexMobile = "^[0-9]{11}$".toRegex()
        val regexAuthNumber = "^[0-9]{6}$".toRegex()

        with(binding) {
            etFindIdName.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validateInput(binding.etFindIdName, binding.tvErrorName, regexName)
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
            etFindPwMobile.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val isValid = validateInput(binding.etFindPwMobile, binding.tvErrorMobile, regexMobile)
                    binding.btnFindIdAuthNumber.isEnabled = isValid
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
            etFindIdAuthNumber.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validateInput(binding.etFindIdAuthNumber, binding.tvErrorAuthNumber, regexAuthNumber)
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
        }
    }

    private fun validateInput(
        textInputLayout: TextInputLayout,
        errorText: TextView,
        regex: Regex
    ): Boolean {
        val inputText = textInputLayout.editText?.text.toString().trim()

        return if (inputText.isEmpty() || !regex.matches(inputText)) {
            textInputLayout.isErrorEnabled = true
            textInputLayout.error = "에러 메시지"
            errorText.visibility = View.VISIBLE
            false
        } else {
            textInputLayout.isErrorEnabled = false
            errorText.visibility = View.GONE
            textInputLayout.error = null
            true
        }
    }

    private fun onClickAuthBtn() {
        with(binding) {
            btnFindIdAuthNumber.setOnClickListener {
                val mobile = etFindPwMobile.editText?.text.toString()
                if (mobile.isEmpty()) {
                    tvErrorMobile.visibility = View.VISIBLE
                    tvErrorMobile.text = getString(R.string.sign_up_error_mobile)
                    tvErrorMobile.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative))
                } else {
                    tvErrorMobile.visibility = View.VISIBLE
                    tvErrorMobile.text = getString(R.string.common_auth_number_check)
                    tvErrorMobile.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive))
                    btnFindIdAuthNumber.setTimer(lifecycleScope, etFindPwMobile, requireContext())
                }
            }
        }
    }

    private fun onClickFindIdBtn() {
        with(binding) {
            btnFindId.setOnClickListener {
                etFindIdName.isEnabled = false
                etFindIdAuthNumber.isEnabled = false
                tvFindId.visibility = View.VISIBLE
            }
        }
    }

    private fun onClickAuthCheckBtn() {
        with(binding) {
            btnFindIdAuthNumberCheck.setOnClickListener {
                val tempPW = "123456"
                val pw = binding.etFindIdAuthNumber.editText?.text.toString()

                if (pw.isEmpty()) {
                    tvErrorAuthNumber.visibility = View.VISIBLE
                    tvErrorAuthNumber.text = getString(R.string.sign_up_error_auth_number)
                    tvErrorAuthNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative))
                } else if (pw == tempPW) {
                    tvErrorAuthNumber.visibility = View.VISIBLE
                    tvErrorAuthNumber.text = getString(R.string.common_auth_number_check_success)
                    tvErrorAuthNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive))
                    btnFindIdAuthNumberCheck.isEnabled = false
                    btnFindId.isEnabled = true
                } else {
                    tvErrorAuthNumber.visibility = View.VISIBLE
                    tvErrorAuthNumber.text = getString(R.string.common_auth_number_check_failure)
                    tvErrorAuthNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative))
                }
            }
        }
    }

    companion object {
        fun newInstance(): FindIdFragment {
            return FindIdFragment().apply {

            }
        }
    }
}