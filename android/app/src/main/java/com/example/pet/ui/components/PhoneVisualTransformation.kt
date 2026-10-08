package com.example.pet.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val masked = buildString {
            append("+7 (")
            digits.forEachIndexed { i, c ->
                if (i == 3) append(") ")
                if (i == 6 || i == 8) append('-')
                append(c)
            }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                4 + offset +
                        (if (offset > 3) 2 else 0) +
                        (if (offset > 6) 1 else 0) +
                        (if (offset > 8) 1 else 0)

            override fun transformedToOriginal(offset: Int): Int {
                var o = offset - 4
                if (offset > 7) o -= minOf(offset - 7, 2)
                if (offset > 12) o -= 1
                if (offset > 15) o -= 1
                return o.coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(masked), mapping)
    }
}