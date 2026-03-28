package com.example.jobfinderapp.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinderapp.views.rv_helpers.ItemDecorationHf
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.databinding.FragmentSavedBinding
import com.example.jobfinderapp.viewModels.SavedFragmentViewModel
import com.example.jobfinderapp.views.rv_adapters.JobAdapter


class SavedFragment : Fragment() {

    private lateinit var binding: FragmentSavedBinding
    private val sfViewModel: SavedFragmentViewModel by activityViewModels()

    private var didRunEnterAnimationForRv = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val extraRvPadding = requireContext().resources.getDimension(R.dimen.rvExtraPadding).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(binding.savedRecyclerView) { v, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(bottom = extraRvPadding + systemBars.bottom)
            windowInsets
        }

        setState()
    }

    private fun setState() {

        val layManager = LinearLayoutManager(requireContext())

        val savedJobsAdapter = JobAdapter(
            onClick = { jobUIM, v ->
                openDetails(jobUIM, v)
            },
            onFavClick = {
                sfViewModel.toggleSaved(it.job)
            }
        )

        val paddingMain = resources.getDimension(R.dimen.dimenForRVItems).toInt()
        val paddingSide = resources.getDimension(R.dimen.dimenForRVItemsSide).toInt()
        val itemDec = ItemDecorationHf(paddingMain, paddingSide)

        binding.savedRecyclerView.layoutManager = layManager
        binding.savedRecyclerView.adapter = savedJobsAdapter

        //Это нужно для того, если фрагмент пересоздаться, чтобы не было добавлено лишних отступов
        if (binding.savedRecyclerView.itemDecorationCount == 0) {
            binding.savedRecyclerView.addItemDecoration(itemDec)
        }


        sfViewModel.savedJobsUI.observe(viewLifecycleOwner) { savedJobList ->

            if (savedJobList.isNullOrEmpty()) {
                binding.savedRecyclerView.visibility = View.GONE
                showEmptyState(binding.noSavedJobsLay, binding.noSavedIv, binding.noSavedTv, binding.noSavedTvDesc)
            } else {
                binding.noSavedJobsLay.visibility = View.GONE
                binding.savedRecyclerView.isVisible = true
                savedJobsAdapter.submitList(savedJobList) {
                    if (!didRunEnterAnimationForRv) {
                        binding.savedRecyclerView.scheduleLayoutAnimation()
                        didRunEnterAnimationForRv = true
                    }
                }
            }

        }

    }

    private fun openDetails(jobUIModel: JobUIModel, v: View) {
        val trName = "job_title${jobUIModel.job.title}"

        val extras = FragmentNavigatorExtras(v to trName)

        val action =
            SavedFragmentDirections.actionSavedFragmentToDetailsFragment(jobUIModel)

        findNavController().navigate(action, extras)
    }


    private fun showEmptyState(
        emptyLay: View,
        imageView: ImageView,
        textView: View,
        textView1: View
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

    }
}