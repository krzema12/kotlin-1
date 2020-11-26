/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.transformers.irToPy

import generated.Python.FunctionDef
import org.jetbrains.kotlin.ir.backend.js.utils.JsGenerationContext
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class IrFunctionToPyTransformer : BaseIrElementToPyNodeTransformer<FunctionDef, JsGenerationContext> {
    override fun visitSimpleFunction(declaration: IrSimpleFunction, context: JsGenerationContext): FunctionDef {
        return translateFunction(declaration, context)
    }

    override fun visitConstructor(declaration: IrConstructor, context: JsGenerationContext): FunctionDef {
        return translateFunction(declaration, context)
    }
}