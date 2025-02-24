package com.protect.jikigo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.protect.jikigo.data.model.Coupon
import com.protect.jikigo.databinding.ItemCouponBinding
import com.protect.jikigo.databinding.ItemCouponHorizontalBinding
import com.protect.jikigo.ui.extensions.applyNumberFormat

class CouponHorizontalAdapter (
    private var items: List<Coupon>,
    private val listener: TravelCouponHorizontalOnClickListener
) : RecyclerView.Adapter<CouponHorizontalAdapter.CouponViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        return CouponViewHolder(
            ItemCouponHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener = { position -> listener.onClickListener(items[position]) }
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateList(newList: List<Coupon>) {
        this.items = newList
        notifyDataSetChanged()
    }


    class CouponViewHolder(
        private val binding: ItemCouponHorizontalBinding,
        private val listener: (Int) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                listener(adapterPosition)
            }
        }
        fun bind(item: Coupon) {
            binding.apply {
                tvCouponName.text = item.couponName
                tvCouponBrand.text = item.couponBrand
                tvCouponPrice.applyNumberFormat(item.couponPrice)
                Glide.with(root.context)
                    .load(item.couponImg)
                    .into(ivCouponDetailThumnail)
            }
        }
    }
}
interface TravelCouponHorizontalOnClickListener {
    fun onClickListener(item: Coupon)
}