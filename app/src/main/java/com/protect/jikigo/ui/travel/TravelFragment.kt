package com.protect.jikigo.ui.travel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.protect.jikigo.R
import com.protect.jikigo.databinding.FragmentTravelBinding
import com.protect.jikigo.ui.extensions.statusBarColor

class TravelFragment : Fragment() {
    private var _binding: FragmentTravelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTravelBinding.inflate(inflater, container, false)
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
        setStatusBar()
        setViewPager()
        moveToMyPage()
    }

    private fun setStatusBar() {
        requireActivity().statusBarColor(R.color.primary)
    }

    private fun setViewPager() {
        binding.apply {
            tabLayoutTravel.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    // 페이지 이동 시 슬라이드 애니메이션 효과 제거
                    vpTravel.setCurrentItem(tab?.position ?: 0, false)

                    // 화면전환 확인 임시 코드
                    val message = when (tab?.position) {
                        0 -> "HOME 탭을 선택했습니다."
                        1 -> "숙박 탭을 선택했습니다."
                        2 -> "레저/티켓 탭을 선택했습니다."
                        3 -> "공연/전시 탭을 선택했습니다."
                        4 -> "여행용품 탭을 선택했습니다."
                        else -> "알 수 없는 탭"
                    }
                    Log.d("TabSelection", message)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })

            // 슬라이드로 화면전환 삭제
            vpTravel.isUserInputEnabled = false

            vpTravel.adapter = TravelViewPagerAdapter(childFragmentManager, lifecycle)

            val tavelTabLayoutMediator = TabLayoutMediator(tabLayoutTravel, vpTravel) { tab, position ->
                // 원래는 position별로 분기하여 처리해주세요
                // 각 탭에 보여줄 문자열을 새롭게 구성해줘야 한다.
                when (position) {
                    0 -> tab.text = "HOME"
                    1 -> tab.text = "숙박"
                    2 -> tab.text = "레저/티켓"
                    3 -> tab.text = "공연/전시"
                    4 -> tab.text = "여행용품"
                }
            }
            tavelTabLayoutMediator.attach()
        }
    }

    private fun moveToMyPage() {
        binding.toolbarTravel.setOnMenuItemClickListener { menu ->
            if (menu.itemId == R.id.menu_my_page) {
                val action = TravelFragmentDirections.actionNavigationTravelToMyPage()
                findNavController().navigate(action)
                true
            } else {
                false
            }
        }
    }

    // ViewPager2 어댑터
    inner class TravelViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        // ViewPager2를 통해 보여줄 프래그먼트의 개수
        override fun getItemCount(): Int {
            return 5
        }

        // position번째에서 사용할 Fragment 객체를 생성해 반환하는 메서드
        override fun createFragment(position: Int): Fragment {
            val newFragment = when (position) {
                0 -> TravelHomeFragment()
                1 -> TravelCouponFragment()
                2 -> TravelCouponFragment()
                3 -> TravelCouponFragment()
                4 -> TravelCouponFragment()
                else -> TravelCouponFragment()
            }
            return newFragment
        }
    }
}