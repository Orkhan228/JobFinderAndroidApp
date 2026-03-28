package com.example.jobfinderapp.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinderapp.App
import com.example.jobfinderapp.views.rv_helpers.ItemDecorationHf
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.databinding.FragmentHomeBinding
import com.example.jobfinderapp.entity.Country
import com.example.jobfinderapp.viewModels.HomeFragViewModel
import com.example.jobfinderapp.views.rv_adapters.JobAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val hfViewModel: HomeFragViewModel by activityViewModels()

    private var didRunEnterAnimationForRv = false

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
        //переделать на переход через navController
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

        val jobAdapter = JobAdapter(onClick = { jobUIModel, v ->
            openDetails(jobUIModel, v)
        }, onFavClick = { jobUIM ->
            hfViewModel.toggleSaved(jobUIM.job)
        })

        val linearLayoutManager = LinearLayoutManager(requireContext())

        val paddingMain = resources.getDimension(R.dimen.dimenForRVItems).toInt()
        val paddingSide = resources.getDimension(R.dimen.dimenForRVItemsSide).toInt()
        val itemDec = ItemDecorationHf(paddingMain, paddingSide)

        binding.hfRecyclerView.layoutManager = linearLayoutManager
        binding.hfRecyclerView.adapter = jobAdapter
        binding.hfRecyclerView.addItemDecoration(itemDec)


        //подписка на изменения списка работ. Так как работаем c ListAdapter, то список обновляем через submitList
        hfViewModel.jobsUIModel.observe(viewLifecycleOwner) { jobUIModelList ->

            val isEmpty = jobUIModelList.isNullOrEmpty()
            val isConnected = hfViewModel.internetState.value ?: true

            binding.noApiDbLay.isVisible = !isConnected && isEmpty
            binding.noSearchResLay.isVisible = isConnected && isEmpty
            binding.hfRecyclerView.isVisible = !isEmpty

            jobAdapter.submitList(jobUIModelList) {
                if (!didRunEnterAnimationForRv) {
                    binding.hfRecyclerView.scheduleLayoutAnimation()
                    didRunEnterAnimationForRv = true
                }
            }

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

    private fun openDetails(jobUIModel: JobUIModel, v: View) {
        val trName = "job_title${jobUIModel.job.title}"

        val extras = FragmentNavigatorExtras(v to trName)

        val action =
            HomeFragmentDirections.actionHomeFragmentToDetailsFragment(jobUIModel)

        findNavController().navigate(action, extras)
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
            }
            false
        }
    }

    companion object {
        const val FILTER_FRAGMENT_TAG = "FilterBottomSheet"
    }
}