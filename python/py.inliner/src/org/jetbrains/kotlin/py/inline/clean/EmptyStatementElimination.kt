/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.py.inline.clean

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.synthetic
import org.jetbrains.kotlin.py.translate.utils.JsAstUtils

internal class EmptyStatementElimination(private val root: JsStatement) {
    private var hasChanges = false

    fun apply(): Boolean {
        object : JsVisitorWithContextImpl() {
            override fun visit(x: JsFunction, ctx: JsContext<*>) = false

            override fun endVisit(x: JsLabel, ctx: JsContext<JsNode>) {
                if (x.synthetic) {
                    if (isEmpty(x.statement)) {
                        ctx.replaceMe(x.statement)
                        hasChanges = true
                    }
                }
            }

            override fun endVisit(x: JsBlock, ctx: JsContext<*>) {
                processStatements(x.statements)
            }

            override fun endVisit(x: JsIf, ctx: JsContext<JsNode>) {
                val thenEmpty = isEmpty(x.thenStatement)
                val elseEmpty = x.elseStatement?.let { isEmpty(it) } ?: true
                when {
                    thenEmpty && elseEmpty -> {
                        hasChanges = true
                        ctx.replaceMe(JsAstUtils.asSyntheticStatement(x.ifExpression))
                    }
                    elseEmpty -> {
                        if (x.elseStatement != null) {
                            hasChanges = true
                            x.elseStatement = null
                        }
                    }
                    thenEmpty -> {
                        hasChanges = true
                        x.thenStatement = x.elseStatement!!
                        x.elseStatement = null
                        x.ifExpression = JsAstUtils.notOptimized(x.ifExpression)
                    }
                }
            }

            override fun endVisit(x: JsTry, ctx: JsContext<JsNode>) {
                val finallyBlock = x.finallyBlock
                if (x.tryBlock.isEmpty) {
                    hasChanges = true
                    ctx.replaceMe(finallyBlock ?: JsEmpty)
                }
            }

            override fun endVisit(x: JsSwitch, ctx: JsContext<JsNode>) {
                for (case in x.cases) {
                    processStatements(case.statements)
                }
                if (x.cases.lastOrNull() is JsDefault && x.cases.dropLast(1).all { it.statements.isEmpty() }
                    || x.cases.all { it.statements.isEmpty() }
                ) {
                    hasChanges = true
                    val replacement = mutableListOf(JsAstUtils.asSyntheticStatement(x.expression))
                    x.cases.lastOrNull()?.apply { replacement.addAll(statements) }
                    ctx.replaceMe(JsBlock(replacement))
                }
            }

            private fun processStatements(statements: MutableList<JsStatement>) {
                for ((index, statement) in statements.withIndex().reversed()) {
                    if (statement is JsEmpty) {
                        statements.removeAt(index)
                        hasChanges = true
                    }
                    else if (statement is JsBlock) {
                        statements.removeAt(index)
                        statements.addAll(index, statement.statements)
                    }
                }
            }

            private fun isEmpty(statement: JsStatement) = statement is JsBlock && statement.isEmpty || statement is JsEmpty
        }.accept(root)
        return hasChanges
    }
}