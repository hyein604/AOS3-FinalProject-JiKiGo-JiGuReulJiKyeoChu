package com.protect.jikigo.ui.travel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.protect.jikigo.R
import com.protect.jikigo.databinding.FragmentTravelCouponBinding

class TravelCouponFragment : Fragment() {
    private lateinit var fragmentTravelCouponBinding: FragmentTravelCouponBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentTravelCouponBinding = FragmentTravelCouponBinding.inflate(inflater)

        fragmentTravelCouponBinding.apply {
            val sortContainer = sortContainer
            val tvSort = tvSort

            sortContainer.setOnClickListener {
                val popupMenu = PopupMenu(requireContext(), it)
                popupMenu.menuInflater.inflate(R.menu.menu_sort_options, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    tvSort.text = item.title
                    true
                }
                popupMenu.show()
            }
        }

        return fragmentTravelCouponBinding.root
    }
}