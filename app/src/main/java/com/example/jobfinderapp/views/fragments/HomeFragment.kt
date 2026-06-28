package com.example.jobfinderapp.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.jobfinderapp.views.rv_helpers.ItemDecorationHf
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.databinding.FragmentHomeBinding
import com.example.jobfinderapp.utils.AppLogger
import com.example.jobfinderapp.utils.ReselectedScroll
import com.example.jobfinderapp.utils.SafeLinearLayoutManager
import com.example.jobfinderapp.viewModels.HomeFragViewModel
import com.example.jobfinderapp.views.activities.MainActivity
import com.example.jobfinderapp.views.rv_adapters.HfJobLoadStateAdapter
import com.example.jobfinderapp.views.rv_adapters.JobAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), ReselectedScroll {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val hfViewModel: HomeFragViewModel by activityViewModels()

    private val jobAdapter by lazy {
        JobAdapter(onClick = { jobUIModel, v ->
            openDetails(jobUIModel, v)
        }, onFavClick = { jobUIM ->
            hfViewModel.toggleSaved(jobUIM.job)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val extraRvPadding = requireContext().resources.getDimension(R.dimen.rvExtraPadding).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(binding.hfRecyclerView) { v, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(bottom = extraRvPadding + systemBars.bottom)
            windowInsets
        }

        animateBtn()

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
            //Когда переделал на переход через navController, в графе поставил настройки запуска как singleTop
            if (findNavController().currentDestination?.id == R.id.homeFragment) {
                val action = HomeFragmentDirections.actionHomeFragmentToFilterBottomSheetFragment()

                (activity as MainActivity).hideBotNavViewExclusive { findNavController().navigate(action) }
            }
        }

        binding.hfSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //принятие ключевых слов, для поиска работы, опять же при нажатии submit, то есть подтверждения, закрывать searchView
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.hfSearchView.clearFocus()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                hfViewModel.onSearchKeyWordsChangedFlow(newText)

                return true
            }
        })

        binding.hfSwipeRefreshLay.setOnRefreshListener {
            jobAdapter.refresh()
        }

        //Специально для paging, при агрессивном скроллинге перехватываем баги
        //Баг был в самой версии paging3 3.3.0 - 3.3.4
        val linearLayoutManager = SafeLinearLayoutManager(requireContext())

        val paddingMain = resources.getDimension(R.dimen.dimenForRVItems).toInt()
        val paddingSide = resources.getDimension(R.dimen.dimenForRVItemsSide).toInt()
        val itemDec = ItemDecorationHf(paddingMain, paddingSide)

        binding.hfRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = jobAdapter.withLoadStateFooter(HfJobLoadStateAdapter({jobAdapter.retry()}))
            addItemDecoration(itemDec)
        }

        binding.hfSwipeRefreshLay.setOnChildScrollUpCallback { _, _ ->
            binding.hfRecyclerView.canScrollVertically(-1)
        }

        postponeEnterTransition()
        observeViewModel()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    hfViewModel.jobUIModelFlow.collectLatest { pagingDataJobs ->
                        jobAdapter.submitData(pagingDataJobs)
                    }
                }

                launch {
                    hfViewModel.networkRestoredEvent.collectLatest {
                        jobAdapter.retry()
                    }
                }

                launch {
                    jobAdapter.loadStateFlow.collectLatest { loadStates ->

                        val isNotLoading = loadStates.refresh is LoadState.NotLoading
                        val isLoading = loadStates.refresh is LoadState.Loading
                        val isError = loadStates.refresh is LoadState.Error
                        val errorState = loadStates.refresh as? LoadState.Error

                        val appendLoading = loadStates.append is LoadState.Loading

                        binding.hfSwipeRefreshLay.isRefreshing = isLoading && !appendLoading

                        if (loadStates.refresh is LoadState.NotLoading && jobAdapter.itemCount > 0) {
                            startPostponedEnterTransition()
                        }

                        if (isLoading && jobAdapter.itemCount == 0) {
                            binding.hfShimmerFrameLay.visibility = View.VISIBLE
                            binding.hfShimmerFrameLay.startShimmer()
                        } else {
                            binding.hfShimmerFrameLay.stopShimmer()
                            binding.hfShimmerFrameLay.visibility = View.GONE
                        }

                        if (isError) {
                            AppLogger.e("HomeFragment", "Error State in RecyclerView", errorState?.error)
                        }

                        binding.noApiDbLay.isVisible = isError && jobAdapter.itemCount == 0
                        binding.noSearchResLay.isVisible = isNotLoading && jobAdapter.itemCount == 0
                    }
                }

                launch {
                    jobAdapter.loadStateFlow
                        .map { it.refresh is LoadState.NotLoading && it.append.endOfPaginationReached && jobAdapter.itemCount == 0 }
                        .distinctUntilChanged()
                        .collect { isEmpty ->
                            binding.noSearchResLay.isVisible = isEmpty
                            binding.hfSwipeRefreshLay.isVisible = !isEmpty
                        }
                }

                launch {
                    hfViewModel.filterBadgeCountFlow.collect { count ->
                        if (count > 0) {
                            binding.filterBadgeTv.text = count.toString()
                            binding.filterBadgeTv.visibility = View.VISIBLE
                        } else {
                            binding.filterBadgeTv.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun openDetails(jobUIModel: JobUIModel, v: View) {
        val trName = "job_title${jobUIModel.job.id}"

        val extras = FragmentNavigatorExtras(v to trName)

        val action =
            HomeFragmentDirections.actionHomeFragmentToDetailsFragment(jobUIModel)

        findNavController().navigate(action, extras)
    }

    private fun observeViewModel() {
        // Слушаем команды (события) от ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var listenToNextRefresh = false // Флаг живет прямо в контексте обработки событий

                // Запускаем параллельный сбор состояний Paging, но связываем его с событиями
                launch {
                    jobAdapter.loadStateFlow.collect { loadStates ->
                        // Если ViewModel приказала скроллить И пагинатор закончил загрузку
                        if (listenToNextRefresh && loadStates.refresh is LoadState.NotLoading) {
                            binding.hfRecyclerView.post {
                                if (_binding != null && jobAdapter.itemCount > 0) {
                                    binding.hfRecyclerView.smoothScrollToPosition(0)
                                }
                            }
                            listenToNextRefresh = false // Задача выполнена, опускаем флаг
                        }
                    }
                }

                // Слушаем сами события
                hfViewModel.scrollToTop.collect {
                    binding.hfRecyclerView.stopScroll()
                    listenToNextRefresh = true
                }

            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun animateBtn() {
        val applyBtn = binding.hfFilterBtn

        applyBtn.setOnTouchListener { v, event ->

            when(event.action) {
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
    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun smoothScrollToStart() {
        binding.hfRecyclerView.smoothScrollToPosition(0)
        binding.hfAppBarLayout.setExpanded(true)
    }
}