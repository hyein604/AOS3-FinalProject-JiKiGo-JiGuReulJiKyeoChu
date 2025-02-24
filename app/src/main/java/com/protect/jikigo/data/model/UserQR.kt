package com.protect.jikigo.data.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserQR(
    // 사용자 고유 ID
    val userId: String = "",
    val userQR: String = "",
    val userQrUse: Boolean = false,
    val userPoint: Int = 0,
    val userQrError: String = "",
    val paymentData: String = getCurrentFormattedDate(), // 현재 날짜를 String으로 저장
    val paymentPrice: Int = 0,
)

// 현재 날짜를 "yyyy/MM/dd HH:mm:ss" 형식으로 반환하는 함수
fun getCurrentFormattedDate(): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date()) // 현재 시간 변환
}