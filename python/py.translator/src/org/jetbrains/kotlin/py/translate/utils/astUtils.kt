/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.translate.utils.jsAstUtils

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.py.translate.context.Namer
import org.jetbrains.kotlin.py.translate.context.TranslationContext
import org.jetbrains.kotlin.types.expressions.OperatorConventions

fun JsFunction.addStatement(stmt: JsStatement) {
    body.statements.add(stmt)
}

fun JsFunction.addParameter(identifier: String, index: Int? = null): JsParameter {
    val name = JsScope.declareTemporaryName(identifier)
    val parameter = JsParameter(name)

    if (index == null) {
        parameters.add(parameter)
    } else {
        parameters.add(index, parameter)
    }

    return parameter
}

/**
 * Tests, if any node containing in receiver's AST matches, [predicate].
 */
fun JsNode.any(predicate: (JsNode) -> Boolean): Boolean {
    val visitor = object : RecursiveJsVisitor() {
        var matched: Boolean = false

        override fun visitElement(node: JsNode) {
            matched = matched || predicate(node)

            if (!matched) {
                super.visitElement(node)
            }
        }
    }

    visitor.accept(this)
    return visitor.matched
}

fun JsExpression.toInvocationWith(
        leadingExtraArgs: List<JsExpression>,
        parameterCount: Int,
        thisExpr: JsExpression
): JsExpression {
    val qualifier: JsExpression
    fun padArguments(arguments: List<JsExpression>) = arguments + (1..(parameterCount - arguments.size))
            .map { Namer.getUndefinedExpression() }

    return when (this) {
        is JsNew -> {
            qualifier = Namer.getFunctionCallRef(constructorExpression)
            // `new A(a, b, c)` -> `A.call($this, a, b, c)`
            JsInvocation(qualifier, listOf(thisExpr) + leadingExtraArgs + arguments).source(source)
        }
        is JsInvocation -> {
            qualifier = getQualifier()
            // `A(a, b, c)` -> `A(a, b, c, $this)`
            JsInvocation(qualifier, leadingExtraArgs + padArguments(arguments) + thisExpr).source(source)
        }
        else -> throw IllegalStateException("Unexpected node type: " + this::class.java)
    }
}

var JsWhile.test: JsExpression
    get() = condition
    set(value) { condition = value }

var JsArrayAccess.index: JsExpression
    get() = indexExpression
    set(value) { indexExpression = value }

var JsArrayAccess.array: JsExpression
    get() = arrayExpression
    set(value) { arrayExpression = value }

var JsConditional.test: JsExpression
    get() = testExpression
    set(value) { testExpression = value }

var JsConditional.then: JsExpression
    get() = thenExpression
    set(value) { thenExpression = value }

var JsConditional.otherwise: JsExpression
    get() = elseExpression
    set(value) { elseExpression = value }

// Extension functions below produce aliased invocations.
fun TranslationContext.invokeKotlinFunction(functionName: String, vararg arguments: JsExpression)
    = JsInvocation(getReferenceToIntrinsic(functionName), arguments.toList())

fun TranslationContext.toByte(expression: JsExpression) = invokeKotlinFunction(OperatorConventions.BYTE.identifier, expression)

fun TranslationContext.toShort(expression: JsExpression) = invokeKotlinFunction(OperatorConventions.SHORT.identifier, expression)

fun TranslationContext.toChar(expression: JsExpression) = invokeKotlinFunction(OperatorConventions.CHAR.identifier, expression)

fun TranslationContext.toLong(expression: JsExpression) = invokeKotlinFunction(OperatorConventions.LONG.identifier, expression)