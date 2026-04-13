package com.langmaster.data.repo

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationPolicyEvaluatorTest {
    @Test
    fun returnsFalse_whenTranslationDisabled() {
        assertFalse(TranslationPolicyEvaluator.shouldUseAi(false, "Hindi", "English"))
    }

    @Test
    fun returnsFalse_whenLanguagesSame() {
        assertFalse(TranslationPolicyEvaluator.shouldUseAi(true, "Hindi", "Hindi"))
    }

    @Test
    fun returnsTrue_whenEnabledAndLanguageDiffers() {
        assertTrue(TranslationPolicyEvaluator.shouldUseAi(true, "Gujarati", "English"))
    }
}
