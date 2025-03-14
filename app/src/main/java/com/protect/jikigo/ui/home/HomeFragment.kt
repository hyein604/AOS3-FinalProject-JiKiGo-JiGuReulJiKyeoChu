package com.protect.jikigo.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.protect.jikigo.R
import com.protect.jikigo.data.model.Store
import com.protect.jikigo.data.repo.StoreRepo
import com.protect.jikigo.databinding.FragmentHomeBinding
import com.protect.jikigo.ui.adapter.HomeAdapter
import com.protect.jikigo.utils.applyNumberFormat
import com.protect.jikigo.utils.applySpannableStyles
import com.protect.jikigo.utils.getUserId
import com.protect.jikigo.utils.statusBarColor
import com.protect.jikigo.ui.viewModel.HomeViewModel
import com.protect.jikigo.ui.viewModel.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), HomeStoreItemClickListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private val homeViewModel by viewModels<HomeViewModel>()

    @Inject
    lateinit var storeRepo: StoreRepo


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.shimmerHomeStore.stopShimmer()
        _binding = null
    }

    private fun setLayout() {
        getUserInfo()
        setRecyclerView()
        setStatusBarColor()
        homeTextSpannable()
        observeNotificationList()
        moveToMyPage()
        moveToNews()
        moveToNotification()
        moveToQR()
        moveToTravel()
        moveToRank()
        moveToPointHistory()
    }

    private fun getUserInfo() {
        lifecycleScope.launch {
            val userId = requireContext().getUserId() ?: ""
            Log.d("HomeFragment", "userId: $userId")
            homeViewModel.getUserInfo(userId)
        }

        homeViewModel.item.observe(viewLifecycleOwner) { userInfo ->
            userInfo?.let {
                binding.tvHomeNickname.text = "${it.userNickName} 님,"
                binding.tvHomeNickname.applySpannableStyles(0, it.userNickName.length, R.color.white)

                if (binding.tvHomePoint.text.toString() != it.userPoint.toString()) { // 중복 업데이트 방지
                    binding.tvHomePoint.applyNumberFormat(it.userPoint)
                }
            } ?: run {
                Log.e("HomeFragment", "UserInfo is null")
            }
        }
    }

    private fun setStatusBarColor() {
        requireActivity().statusBarColor(R.color.primary)
    }

    private fun moveToNotification() {
        binding.viewNoticeMore.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToNotification()
            findNavController().navigate(action)
        }
    }

    private fun moveToNews() {
        with(binding) {
            val messages = listOf("환경", "여행", "건강")
            listOf(viewHomeEnvironment, viewHomeTravel, viewHomeHealth).forEachIndexed { index, view ->
                view.setOnClickListener {
                    val action = HomeFragmentDirections.actionNavigationHomeToNews(messages[index])
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun moveToPointHistory() {
        binding.viewPoint.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToPointHistory()
            findNavController().navigate(action)
        }
    }

    private fun moveToQR() {
        binding.ivHomeQr.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToPaymentQR()
            findNavController().navigate(action)
        }
    }

    private fun moveToMyPage() {
        binding.toolbarHome.setOnMenuItemClickListener { menu ->
            if (menu.itemId == R.id.menu_my_page) {
                val action = HomeFragmentDirections.actionNavigationHomeToMyPage()
                findNavController().navigate(action)
                true
            } else {
                false
            }
        }
    }

    private fun moveToTravel() {
        binding.viewStoreMore.setOnClickListener {
            val bottomNavHome = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav_home)
            bottomNavHome.selectedItemId = R.id.navigation_travel
        }
    }

    private fun moveToRank() {
        binding.tvHomeClickRank.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToRanking()
            findNavController().navigate(action)
        }
    }

    private fun homeTextSpannable() {
        // 닉네임의 길이를 넣어줌
        // binding.tvHomeNickname.applySpannableStyles(0, 3, R.color.white)
        binding.tvHomeClickRank.applySpannableStyles(0, binding.tvHomeClickRank.length(), R.color.black, true, true)
    }


    private fun observeNotificationList() {
        notificationViewModel.notificationListHomeFragment.observe(viewLifecycleOwner) { notificationList ->
            if (notificationList.isNotEmpty()) {
                val topNotices = notificationList.take(3) // 상위 3개만 가져오기
                with(binding) {
                    tvHomeNotice1.text = topNotices.getOrNull(0)?.title ?: ""
                    tvHomeNotice2.text = topNotices.getOrNull(1)?.title ?: ""
                    tvHomeNotice3.text = topNotices.getOrNull(2)?.title ?: ""

                    listOf(tvHomeNotice1, tvHomeNotice2, tvHomeNotice3).forEachIndexed { index, textView ->
                        textView.setOnClickListener {
                            val action = HomeFragmentDirections
                                .actionNavigationHomeToNotificationDetail(topNotices[index])
                            findNavController().navigate(action)
                        }
                    }
                }
            } else {
                // 빈 리스트일 때는 UI를 초기화하거나 처리
                with(binding) {
                    tvHomeNotice1.text = "로딩중..."
                    tvHomeNotice2.text = "로딩중..."
                    tvHomeNotice3.text = "로딩중..."
                }
            }
        }
    }

    private fun setRecyclerView() {
        binding.shimmerHomeStore.startShimmer()
        binding.shimmerHomeStore.visibility = View.VISIBLE
        binding.rvHomeStore.visibility = View.GONE

        lifecycleScope.launch {
            val storeList = storeRepo.getAllStore().take(3)

            if (!isAdded || view == null) return@launch

            val adapter = HomeAdapter(storeList, this@HomeFragment)
            binding.rvHomeStore.adapter = adapter

            stopShimmer()
        }
    }

    private fun stopShimmer() {
        if (!isAdded || view == null) return

        binding.shimmerHomeStore.stopShimmer()
        binding.shimmerHomeStore.visibility = View.GONE
        binding.rvHomeStore.visibility = View.VISIBLE
    }

    override fun onClickStore(store: Store) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(store.storeUrl))
        startActivity(intent)
    }
}