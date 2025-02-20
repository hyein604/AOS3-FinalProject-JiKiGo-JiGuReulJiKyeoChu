package com.protect.jikigo.ui.home.my_page



import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.protect.jikigo.LoginActivity
import com.protect.jikigo.R
import com.protect.jikigo.databinding.FragmentMyPageBinding
import com.protect.jikigo.ui.extensions.clearUserId
import com.protect.jikigo.ui.extensions.statusBarColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Date
import kotlinx.coroutines.launch


class MyPageFragment : Fragment() {
    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyPageViewModel by viewModels()

    private lateinit var requestPermissions: ActivityResultLauncher<Set<String>>
    private lateinit var healthConnectClient: HealthConnectClient

    val permission =
        setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class)
        )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)

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
        moveToEditProfile()
        moveToPointHistory()
        moveToCouponBox()
        onClickToolbar()
        lifecycleScope.launch {
            checkInstallHC()
        }
        viewModel.count.observe(viewLifecycleOwner) {
            binding.tvMyPageWalkCount.text = "${viewModel.count.value!!.toInt()} 걸음"
            binding.tvMyPageWalkKcal.text = "${viewModel.count.value!!.toInt() * 0.04}kcal"
            val date = Date(System.currentTimeMillis())
            val simpleDateFormat = SimpleDateFormat("MM-dd")
            val now = simpleDateFormat.format(date)
            binding.tvMyPageWalkDate.text = "$now"
        }
    }

    private fun setStatusBar() {
        requireActivity().statusBarColor(R.color.white)
    }

    private fun moveToEditProfile() {
        binding.btnMyPageProfileEdit.setOnClickListener {
            val action = MyPageFragmentDirections.actionMyPageToProfileEdit()
            findNavController().navigate(action)
        }
    }

    private fun moveToPointHistory() {
        binding.viewMyPagePoint.setOnClickListener {
            val action = MyPageFragmentDirections.actionMyPageToPointHistory()
            findNavController().navigate(action)
        }
    }

    private fun moveToCouponBox() {
        binding.viewMyPageCoupon.setOnClickListener {
            val action = MyPageFragmentDirections.actionMyPageToCouponBox()
            findNavController().navigate(action)
        }
    }

    private fun onClickToolbar() {
        binding.toolbarMyPage.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    /*
    걸음 수
     */

    private fun movePermissionSetting(context: Context) {
        val packageName = "com.google.android.apps.healthdata"
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)

        if (intent != null) {
            context.startActivity(intent)
            Toast.makeText(context, "앱 권한 → '걸음 수'를 활성화 해주세요.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "설정 화면을 여는 데 실패했습니다. 직접 앱 권한을 확인해주세요.", Toast.LENGTH_LONG).show()
        }
    }



    //헬스 커넥트 플레이스토어 이동
    private fun openPlayStoreForHealthConnect(){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            setPackage("com.android.vending")
        }
        startActivity(intent)
    }

    //헬스커넥트 설치, 버전, 권한 여부 확인
    private fun checkInstallHC() {
        val providerPackageName = "com.google.android.apps.healthdata"
        val availabilityStatus = HealthConnectClient.getSdkStatus(requireContext(), providerPackageName)

        // 헬스 커넥트 앱이 없거나 업데이트가 필요할 때 Play Store 이동
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE ||
            availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            Toast.makeText(requireContext(), "헬스 커넥트 앱을 설치 또는 업데이트 해주세요", Toast.LENGTH_SHORT).show()
            openPlayStoreForHealthConnect()
            return
        }

        healthConnectClient = HealthConnectClient.getOrCreate(requireContext())

        // 권한(permission) 확인
        val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
        requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(permission)) {
                // 권한이 부여된 경우
                Log.d("PermissionO", "$healthConnectClient")
                lifecycleScope.launch {
                    readStepsByTimeRange()
                }
            } else {
                // 권한이 거부된 경우 처리
                Log.d("PermissionX", "$healthConnectClient")

                // "다시 묻지 않음"을 선택했는지 확인
                val shouldShowRationale = permission.any {
                    shouldShowRequestPermissionRationale(it)
                }

                if (shouldShowRationale) {
                    // 권한을 다시 요청
                    requestPermissions.launch(permission)
                } else {
                    // "다시 묻지 않음"을 선택했을 경우 -> 설정 화면으로 이동
                    movePermissionSetting(requireContext())
                }
            }
        }

        // 권한 요청 실행
        requestPermissions.launch(permission)
    }


    private suspend fun readStepsByTimeRange() {
        val now: LocalDateTime = LocalDateTime.now()
        val startOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT)

        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )

            if(response.records.isEmpty()) {
                //테스트용 : 헬스 커넥트에 데이터 쓰기
                val startTime = Instant.now().minusSeconds(3600)
                val endTime = Instant.now()
                val stepsRecord =
                    StepsRecord(startTime, ZoneOffset.UTC, endTime, ZoneOffset.UTC, 10000)
                healthConnectClient.insertRecords(listOf(stepsRecord))
            }

            viewModel.count.value = response.records[0].count

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "$e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onClickLogOut() {
        binding.btnMyPageLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                requireContext().clearUserId()
                requireActivity().finish()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }


}