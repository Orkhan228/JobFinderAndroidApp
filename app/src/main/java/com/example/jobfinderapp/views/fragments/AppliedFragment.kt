package com.example.jobfinderapp.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.databinding.FragmentAppliedBinding
import com.example.jobfinderapp.utils.ReselectedScroll
import com.example.jobfinderapp.viewModels.AppliedFragmentViewModel
import com.example.jobfinderapp.views.rv_adapters.AppliedJobAdapter
import com.example.jobfinderapp.views.rv_helpers.ItemDecorationHf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppliedFragment : Fragment(), ReselectedScroll {

    private lateinit var binding: FragmentAppliedBinding
    private val viewModel: AppliedFragmentViewModel by viewModels()

    private var didRunEnterAnimationForRv = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAppliedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val extraRvPadding = requireContext().resources.getDimension(R.dimen.rvExtraPadding).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(binding.appliedRecyclerView) { v, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(bottom = extraRvPadding + systemBars.bottom)
            windowInsets
        }

        //Устанавливаем слушатель результата, для соответствущего requestKey
        setFragmentResultListener(WithdrawDialogFragment.WITHDRAW_REQUEST_KEY) { _, bundle ->
            val isConfirmed = bundle.getBoolean(WithdrawDialogFragment.WITHDRAW_BUNDLE_KEY)

            if (isConfirmed) {
                viewModel.confirmWithdraw()
            }
        }

        setRecycler()
        animateBtn(binding.noAppliedJobsBtn)
    }

    private fun setRecycler() {
        val layoutManager = LinearLayoutManager(requireContext())
        val adapter = AppliedJobAdapter(
            onClick = { jobUiM, v ->
                openDetails(jobUiM, v)
            },
            withDrawClick = {

                if (findNavController().currentDestination?.id == R.id.appliedFragment) {
                    viewModel.savePendingJob(it)

                    val action = AppliedFragmentDirections.actionAppliedFragmentToWithdrawDialogFragment()
                    findNavController().navigate(action)
                }

            }
        )

        val paddingMain = resources.getDimension(R.dimen.dimenForRVItems).toInt()
        val paddingSide = resources.getDimension(R.dimen.dimenForRVItemsSide).toInt()
        val itemDec = ItemDecorationHf(paddingMain, paddingSide)

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(findNavController().graph.startDestinationId, saveState = true, inclusive = false)
            .build()

        binding.appliedRecyclerView.layoutManager = layoutManager
        binding.appliedRecyclerView.adapter = adapter
        binding.appliedRecyclerView.addItemDecoration(itemDec)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appliedJobsFlow.collect { jobsUIModelList ->

                    val listState = jobsUIModelList.isEmpty()

                    if (listState) {
                        showEmptyState(binding.noAppliedJobsLay, binding.noAppliedJobsIv,
                            binding.noAppliedJobsTv, binding.noAppliedJobsTv1, binding.noAppliedJobsBtn)
                    }
                    else {
                        binding.noAppliedJobsLay.visibility = View.GONE
                        binding.appliedRecyclerView.visibility = View.VISIBLE
                    }

                    adapter.submitList(jobsUIModelList){
                        if (!didRunEnterAnimationForRv) {
                            binding.appliedRecyclerView.scheduleLayoutAnimation()
                            didRunEnterAnimationForRv = true
                        }
                    }

                }
            }
        }

        binding.noAppliedJobsBtn.setOnClickListener {
            findNavController().navigate(R.id.homeFragment, null, options)
        }

    }


    //Вот здесь есть потенциальный баг, при быстром нажатии после удаления вакансии из Applied.
    private fun openDetails(jobUIModel: JobUIModel, v: View) {
        val trName = "job_title${jobUIModel.job.id}"

        val extras = FragmentNavigatorExtras(v to trName)

        val action = AppliedFragmentDirections.actionAppliedFragmentToDetailsFragment(jobUIModel)

        findNavController().navigate(action, extras)
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

    private fun showEmptyState(
        emptyLay: View,
        imageView: ImageView,
        textView: View,
        textView1: View,
        browseBtn: View
    ) {

        if (emptyLay.isVisible && emptyLay.alpha == 1f) return

        emptyLay.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        imageView.apply {
            alpha = 0f
            scaleX = 0.3f
            scaleY = 0.3f
        }

        textView.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        textView1.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        browseBtn.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        emptyLay.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(280)
            .start()

        imageView.animate()
            .alpha(1f)
            .setDuration(180)
            .start()

        SpringAnimation(imageView, SpringAnimation.SCALE_X, 1f).apply {
            spring = SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            }
            start()
        }

        SpringAnimation(imageView, SpringAnimation.SCALE_Y, 1f).apply {
            spring = SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            }
            start()
        }

        textView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)

        textView1.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)

        browseBtn.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)

    }

    override fun smoothScrollToStart() {
        binding.appliedRecyclerView.smoothScrollToPosition(0)
    }

}