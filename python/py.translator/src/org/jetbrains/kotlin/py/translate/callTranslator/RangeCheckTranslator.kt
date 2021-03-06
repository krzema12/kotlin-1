/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.py.translate.callTranslator

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.js.backend.ast.JsExpression
import org.jetbrains.kotlin.js.backend.ast.JsInvocation
import org.jetbrains.kotlin.js.backend.ast.JsNameRef
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import org.jetbrains.kotlin.py.translate.context.TranslationContext
import org.jetbrains.kotlin.py.translate.general.Translation
import org.jetbrains.kotlin.py.translate.utils.JsAstUtils
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall

class RangeCheckTranslator(private val context: TranslationContext) {
    fun translateAsRangeCheck(resolvedCall: ResolvedCall<out FunctionDescriptor>, receivers: ExplicitReceivers): JsExpression? {
        val calledFunction = resolvedCall.resultingDescriptor
        if (calledFunction.name.asString() != "contains" || calledFunction.containingDeclaration !is ClassDescriptor) return null

        val invocation = (receivers.extensionOrDispatchReceiver as? JsExpression.JsExpressionHasArguments) ?: return null
        val (rangeType, rangeKind) = invocation.range ?: return null
        var (lower, upper) = when {
            rangeKind == RangeKind.UNTIL || rangeType == RangeType.INT -> {
                if (invocation.arguments.size != 2) return null
                invocation.arguments
            }
            else -> {
                if (invocation !is JsInvocation) return null
                val qualifier = invocation.qualifier as? JsNameRef ?: return null
                val receiver = qualifier.qualifier ?: return null
                if (invocation.arguments.size != 1) return null
                listOf(receiver, invocation.arguments.single())
            }
        }
        lower = context.cacheExpressionIfNeeded(lower)
        upper = context.cacheExpressionIfNeeded(upper)

        val subjectPsi = resolvedCall.valueArguments.values.singleOrNull()?.arguments?.singleOrNull()?.getArgumentExpression() ?: return null
        val subject = context.cacheExpressionIfNeeded(Translation.translateAsExpression(subjectPsi, context))

        return when (rangeType) {
            RangeType.INT -> translateAsIntRangeCheck(lower, upper, rangeKind, subject)
            RangeType.LONG -> translateAsLongRangeCheck(lower, upper, rangeKind, subject)
        }
    }

    private fun translateAsIntRangeCheck(lower: JsExpression, upper: JsExpression, kind: RangeKind, subject: JsExpression): JsExpression {
        val lowerCheck = JsAstUtils.lessThanEq(lower, subject)
        val upperCheck = when (kind) {
            RangeKind.RANGE_TO -> JsAstUtils.lessThanEq(subject, upper)
            RangeKind.UNTIL -> JsAstUtils.lessThan(subject, upper)
        }
        lowerCheck.synthetic = true
        upperCheck.synthetic = true
        return JsAstUtils.and(lowerCheck, upperCheck).apply { synthetic = true }
    }

    private fun translateAsLongRangeCheck(lower: JsExpression, upper: JsExpression, kind: RangeKind, subject: JsExpression): JsExpression {
        val lowerCheck = JsAstUtils.invokeMethod(lower, "lessThanOrEqual", subject).apply { sideEffects = SideEffectKind.PURE }
        val upperCheck = when (kind) {
            RangeKind.RANGE_TO -> JsAstUtils.invokeMethod(subject, "lessThanOrEqual", upper)
            RangeKind.UNTIL -> JsAstUtils.invokeMethod(subject, "lessThan", upper)
        }
        upperCheck.sideEffects = SideEffectKind.PURE
        return JsAstUtils.and(lowerCheck, upperCheck)
    }
}


