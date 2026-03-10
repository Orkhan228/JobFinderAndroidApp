package com.example.jobfinderapp.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinderapp.views.rv_helpers.ItemDecorationHf
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.JobWithSaved
import com.example.jobfinderapp.databinding.FragmentSavedBinding
import com.example.jobfinderapp.viewModels.SavedFragmentViewModel
import com.example.jobfinderapp.views.rv_adapters.JobAdapter


class SavedFragment : Fragment() {

    private lateinit var binding: FragmentSavedBinding
    private val sfViewModel: SavedFragmentViewModel by activityViewModels()

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

        setState()
    }

    private fun setState() {

        val layManager = LinearLayoutManager(requireContext())

        val savedJobsAdapter = JobAdapter(
            onClick = { jobWS ->
                val action =
                    SavedFragmentDirections.actionSavedFragmentToDetailsFragment(jobWS)

                findNavController().navigate(action)
            },
            onFavClick = {
                sfViewModel.toggleSaved(it.job)
            }
        )

        val dimen = resources.getDimension(R.dimen.dimenForRVItems).toInt()
        val itemDec = ItemDecorationHf(dimen)

        binding.savedRecyclerView.layoutManager = layManager
        binding.savedRecyclerView.adapter = savedJobsAdapter

        //Это нужно для того, если фрагмент пересоздаться, чтобы не было добавлено лишних отступов
        if (binding.savedRecyclerView.itemDecorationCount == 0) {
            binding.savedRecyclerView.addItemDecoration(itemDec)
        }


        sfViewModel.savedJobsUI.observe(viewLifecycleOwner) { savedJobList ->

            if (savedJobList.isEmpty()) {
                binding.savedRecyclerView.visibility = View.GONE
                binding.noSavedJobsLay.isVisible = true
            } else {
                binding.noSavedJobsLay.visibility = View.GONE
                binding.savedRecyclerView.isVisible = true
                savedJobsAdapter.submitList(savedJobList)
            }

        }

    }
}