package com.protect.jikigo.data.repo

import android.content.Context
import android.util.Log
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginRepo @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun kakaoLogin() = suspendCoroutine { coroutine ->
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                coroutine.resume(false)
                Log.e("kakaoLogin", "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                coroutine.resume(true)
                Log.i("kakaoLogin", "카카오계정으로 로그인 성공 ${token.accessToken}")
            } else {
                // 토큰이 null이고 에러도 null인 경우 (취소 등)
                Log.d("kakaoLogin", "카카오 로그인 취소됨")
                coroutine.resume(false)
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    coroutine.resume(false)
                    Log.e("kakaoLogin", "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        coroutine.resume(false)
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    Log.i("kakaoLogin", "카카오톡으로 로그인 성공 ${token.accessToken}")
                    coroutine.resume(true)
                } else {
                    coroutine.resume(false)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    suspend fun getUserInfo(): NidProfileMap? {
        return suspendCancellableCoroutine {
            NidOAuthLogin().getProfileMap(object : NidProfileCallback<NidProfileMap> {
                override fun onSuccess(result: NidProfileMap) {
                    result.profile?.forEach { key, value ->
                        Log.d("naver", "$key: $value")
                    } ?: Log.d("naver", "프로필 없음")
                    it.resume(result, null)

                }

                override fun onError(errorCode: Int, message: String) {
                    onFailure(errorCode, message)
                    it.resume(null, null)
                }

                override fun onFailure(httpStatus: Int, message: String) {
                    it.resume(null, null)
                    Log.d("naver", "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                    Log.d("naver", "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
                }
            })
        }
    }
}