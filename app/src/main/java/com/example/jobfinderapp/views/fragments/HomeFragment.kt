package com.example.jobfinderapp.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinderapp.App
import com.example.jobfinderapp.views.rv_helpers.ItemDecorationHf
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.R
import com.example.jobfinderapp.databinding.FragmentHomeBinding
import com.example.jobfinderapp.entity.Country
import com.example.jobfinderapp.viewModels.HomeFragViewModel
import com.example.jobfinderapp.views.rv_adapters.JobAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val hfViewModel: HomeFragViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Чтобы при нажатии на весь SearchView, откывалась клава.
        binding.hfSearchView.setOnClickListener {
            binding.hfSearchView.isIconified = false
        }

        //чтобы при тапе, вне searchView, он закрывался
        binding.root.setOnClickListener {
            binding.hfSearchView.clearFocus()
        }

        //Open filter modal bottom sheet
        binding.hfFilterBtn.setOnClickListener {
            //Тут происходит проверка, если есть такой фрагмент с определенным тэгом, то не надо создавать еще один.
            if (childFragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG) == null) {
                val filterBotSheet = FilterBottomSheet()
                filterBotSheet.show(childFragmentManager, FILTER_FRAGMENT_TAG)
            }
        }


        binding.hfSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //принятие ключевых слов, для поиска работы, опять же при нажатии submit, то есть подтверждения, закрывать searchView
            override fun onQueryTextSubmit(query: String?): Boolean {
                println("!!! textSubmit")

                if (query.isNullOrBlank()) return true

                hfViewModel.onSearchKeyWordsChanged(query)

                binding.hfSearchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                println("!!! textChange")
                return true
            }

        })

        val jobAdapter = JobAdapter(onClick = { jobWithSaved ->

            val action =
                HomeFragmentDirections.actionHomeFragmentToDetailsFragment(jobWithSaved)

            findNavController().navigate(action)

        }, onFavClick = { jobWS ->
            hfViewModel.toggleSaved(jobWS.job)
        })

        val linearLayoutManager = LinearLayoutManager(requireContext())

        val paddingMain = resources.getDimension(R.dimen.dimenForRVItems).toInt()
        val itemDec = ItemDecorationHf(paddingMain)

        binding.hfRecyclerView.layoutManager = linearLayoutManager
        binding.hfRecyclerView.adapter = jobAdapter
        binding.hfRecyclerView.addItemDecoration(itemDec)


        //подписка на изменения списка работ. Так как работаем c ListAdapter, то список обновляем через submitList
        hfViewModel.jobsWithSaved.observe(viewLifecycleOwner) { jobWithSavedList ->

            val listState = jobWithSavedList.isNullOrEmpty()

            binding.noApiDbLay.isVisible = listState
            binding.hfRecyclerView.isVisible = !listState

            jobAdapter.submitList(jobWithSavedList)
        }


        //подписка на mediatorLiveData, для показа количества параметров в краю фильтра.
        hfViewModel.filterBadgeCount.observe(viewLifecycleOwner) { count ->
            if (count > 0) {
                binding.filterBadgeTv.text = count.toString()
                binding.filterBadgeTv.visibility = View.VISIBLE
            } else {
                binding.filterBadgeTv.visibility = View.GONE
            }
        }

        //Реализация пагинации
        binding.hfRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int,
            ) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy < 0) return

                val layoutManager = binding.hfRecyclerView.layoutManager as LinearLayoutManager

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!hfViewModel.isLoading &&
                    !hfViewModel.isLastPage &&
                    (firstVisibleItemPosition + visibleItemCount >= totalItemCount - 5)) {
                    hfViewModel.loadNextPage()
                }
            }
        })

    }

    companion object {
        const val FILTER_FRAGMENT_TAG = "FilterBottomSheet"
    }
}