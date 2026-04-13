package com.langmaster.data.service

data class OtpRequest(val phone: String)
data class OtpRequestResponse(val ok: Boolean, val otpPreview: String?)
data class OtpVerifyRequest(val phone: String, val otp: String)
data class OtpVerifyResponse(val ok: Boolean, val token: String?)
data class PinRegisterRequest(val phone: String, val pin: String)
data class PinLoginRequest(val phone: String, val pin: String)
data class PinAuthResponse(val ok: Boolean, val token: String? = null, val error: String? = null)
