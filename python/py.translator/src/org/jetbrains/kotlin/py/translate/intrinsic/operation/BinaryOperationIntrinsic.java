/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.py.translate.intrinsic.operation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.js.backend.ast.JsExpression;
import org.jetbrains.kotlin.py.translate.context.TranslationContext;
import org.jetbrains.kotlin.psi.KtBinaryExpression;
import org.jetbrains.kotlin.py.translate.context.TranslationContext;

@FunctionalInterface
public interface BinaryOperationIntrinsic {
    @NotNull
    JsExpression invoke(
            @NotNull KtBinaryExpression expression,
            @NotNull JsExpression left,
            @NotNull JsExpression right,
            @NotNull TranslationContext context
    );
}
