package com.protect.jikigo.ui.home.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.protect.jikigo.databinding.FragmentNewsAllBinding
import android.util.Log
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.protect.jikigo.data.RetrofitClient
import com.protect.jikigo.data.NewsResponse
import com.protect.jikigo.R
import com.protect.jikigo.data.NewsItem
import com.protect.jikigo.ui.adapter.NewsAllBannerAdapter
import com.protect.jikigo.utils.cleanHtml
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlinx.coroutines.*

class NewsAllFragment : Fragment() {
    private var _binding: FragmentNewsAllBinding? = null
    private val binding get() = _binding!!
    private var category: String? = null
    private var newsCall: Call<NewsResponse>? = null

    private val bannerImages = listOf(
        R.drawable.img_news_all_banner_1,
        R.drawable.img_news_all_banner_2,
        R.drawable.img_news_all_banner_3
    )

    // 배너 자동 넘기기에 필요한 요소들
    private val handler = Handler(Looper.getMainLooper())
    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            binding?.let {
                val nextItem = (it.vpNewsAllBanner.currentItem + 1) % bannerImages.size
                Log.d("adapter", "currentItem Index : ${it.vpNewsAllBanner.currentItem}")
                it.vpNewsAllBanner.setCurrentItem(nextItem, true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString(NewsAllFragment.Companion.ARG_CATEGORY) // 프래그먼트에 전달된 카테고리 가져오기
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 네트워크 요청 취소
        newsCall?.cancel()
        newsCall = null

        // 핸들러 콜백 제거
        handler.removeCallbacks(autoSlideRunnable)

        // 바인딩 해제
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뉴스 검색 실행
        category?.let { fetchNews(it) }
        // 띠 배너
        setupHomeBannerUI()

    }


    // 뉴스 날짜 포맷을 변경하는 함수
    private fun formatDate(pubDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy/M/d HH:mm", Locale.ENGLISH)
            val date = inputFormat.parse(pubDate)
            date?.let { outputFormat.format(it) } ?: pubDate
        } catch (e: Exception) {
            pubDate // 변환 실패 시 원본 반환
        }
    }

    // 뉴스 링크를 열기 위한 함수
    private fun openNewsLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun fetchNews(query: String) {
        val call = RetrofitClient.instance.searchNews(query)
        newsCall = call

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.items?.let { newsList ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val filteredNews = newsList.map { newsItem ->
                                async {
                                    val imageUrl = fetchNewsImageAsync(newsItem.link)
                                    newsItem.copy(imageUrl = imageUrl)
                                }
                            }.awaitAll().filter { it.imageUrl != null }

                            withContext(Dispatchers.Main) {
                                if (_binding != null) updateUI(filteredNews)
                            }
                        }
                    }
                } else {
                    Log.e("News", "API 호출 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                if (!call.isCanceled) {
                    Log.e("News", "네트워크 오류: ${t.message}")
                }
            }
        })
    }

    // 웹사이트의 Open Graph 태그에서 이미지 URL 가져오기
    private suspend fun fetchNewsImageAsync(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(url).get()
                val imageUrl = doc.select("meta[property=og:image]").attr("content")
                if (imageUrl.startsWith("http://")) imageUrl.replace("http://", "https://") else imageUrl
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun updateUI(newsList: List<NewsItem>) {
        if (newsList.size >= 3) {
            binding?.let { bind ->
                // 첫 번째 뉴스
                bind.tvNewsAllFirstTitle.text = newsList[0].title.cleanHtml()
                bind.tvNewsAllFirstDate.text = formatDate(newsList[0].pubDate)
                Glide.with(bind.ivContentNewsAllFirstImage.context)
                    .load(newsList[0].imageUrl)
                    .into(bind.ivContentNewsAllFirstImage)
                bind.ivContentNewsAllFirstImage.setOnClickListener {
                    openNewsLink(newsList[0].link)
                }

                // 두 번째 뉴스
                bind.tvNewsAllSecondTitle.text = newsList[1].title.cleanHtml()
                bind.tvNewsAllSecondDate.text = formatDate(newsList[1].pubDate)
                Glide.with(bind.ivContentNewsAllSecondImage.context)
                    .load(newsList[1].imageUrl)
                    .into(bind.ivContentNewsAllSecondImage)
                bind.ivContentNewsAllSecondImage.setOnClickListener {
                    openNewsLink(newsList[1].link)
                }

                // 세 번째 뉴스
                bind.tvNewsAllThirdTitle.text = newsList[2].title.cleanHtml()
                bind.tvNewsAllThirdDate.text = formatDate(newsList[2].pubDate)
                Glide.with(bind.ivContentNewsAllThirdImage.context)
                    .load(newsList[2].imageUrl)
                    .into(bind.ivContentNewsAllThirdImage)
                bind.ivContentNewsAllThirdImage.setOnClickListener {
                    openNewsLink(newsList[2].link)
                }
            }
        }
    }

    // 배너화면 설정
    private fun setupHomeBannerUI() {
        val bannerAdapter = NewsAllBannerAdapter(bannerImages)
        binding.vpNewsAllBanner.adapter = bannerAdapter
        // 자동 슬라이드 시작
        handler.postDelayed(autoSlideRunnable, 1500)

        // 사용자가 직접 터치하면 자동 슬라이드 멈추기
        binding.vpNewsAllBanner.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING) {
                    handler.removeCallbacks(autoSlideRunnable) // 터치 시 자동 슬라이드 멈춤
                } else if (state == androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE) {
                    handler.postDelayed(autoSlideRunnable, 1500) // 다시 자동 슬라이드 시작
                }
            }
        })
    }

    // 메모리 누수 방지를 위해 onDestroy에서 handler 해제
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoSlideRunnable)
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        // 카테고리를 받아 프래그먼트 인스턴스를 생성하는 함수
        fun newInstance(category: String) = NewsAllFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_CATEGORY, category)
            }
        }
    }
}