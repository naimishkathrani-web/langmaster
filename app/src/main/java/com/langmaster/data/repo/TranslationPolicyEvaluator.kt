package com.langmaster.data.repo

object TranslationPolicyEvaluator {
    fun shouldUseAi(
        translationEnabled: Boolean,
        listenerLanguage: String?,
        sourceLanguage: String?
    ): Boolean {
        if (!translationEnabled) return false
        if (listenerLanguage.isNullOrBlank() || sourceLanguage.isNullOrBlank()) return false
        return !listenerLanguage.equals(sourceLanguage, ignoreCase = true)
    }
}
