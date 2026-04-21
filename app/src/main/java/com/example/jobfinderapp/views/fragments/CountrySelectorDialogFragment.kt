package com.example.jobfinderapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinderapp.R
import com.example.jobfinderapp.databinding.FragmentCountrySelectorDialogBinding
import com.example.jobfinderapp.entity.Country
import com.example.jobfinderapp.utils.JobCountries
import com.example.jobfinderapp.views.rv_adapters.CountrySelectorAdapter
import com.example.jobfinderapp.views.rv_helpers.ItemDecorationHf
import com.google.android.material.transition.MaterialElevationScale


class CountrySelectorDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentCountrySelectorDialogBinding
    private val args: CountrySelectorDialogFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialElevationScale(true).apply {
            duration = 320
        }

        returnTransition = MaterialElevationScale(false).apply {
            duration = 150
        }

    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(), // 95% ширины экрана
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding = FragmentCountrySelectorDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animateBtn(binding.scCloseBtn)
        binding.scCloseBtn.setOnClickListener {
            dismiss()
        }

        setupRecycler()
    }

    private fun setupRecycler() {
        val layoutManager = LinearLayoutManager(requireContext())
        val adapter = CountrySelectorAdapter(
            onRootClick = {
                sendResult(it)
            },
            selectedCountryCode = args.selectedCountryCode
        )
        val pM = resources.getDimension(R.dimen.remindersRVPaddingSides).toInt()
        val pS = resources.getDimension(R.dimen.dimenForRVItemsSide).toInt()
        val itemDec = ItemDecorationHf(pM, pS)

        val countriesList = JobCountries.countriesList
        adapter.submitList(countriesList)

        binding.scRecyclerView.layoutManager = layoutManager
        binding.scRecyclerView.adapter = adapter
        binding.scRecyclerView.addItemDecoration(itemDec)
    }

    private fun sendResult(country: Country) {
        val bundle = bundleOf(
            BUNDLE_KEY_COUNTRY to country.code
        )
        setFragmentResult(REQUEST_KEY, bundle)

        dismiss()
    }

    private fun animateBtn(view: View) {

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.isPressed = true

                    v.animate()
                        .scaleX(0.86f)
                        .scaleY(0.86f)
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

    companion object {
        const val REQUEST_KEY = "fragment_result_key"
        const val BUNDLE_KEY_COUNTRY = "selected_country_code"
    }
}