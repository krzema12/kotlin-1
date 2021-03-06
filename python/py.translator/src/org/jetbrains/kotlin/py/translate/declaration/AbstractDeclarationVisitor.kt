/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.py.translate.declaration

import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.isOverridable
import org.jetbrains.kotlin.js.backend.ast.JsExpression
import org.jetbrains.kotlin.js.backend.ast.JsName
import org.jetbrains.kotlin.py.translate.context.TranslationContext
import org.jetbrains.kotlin.py.translate.expression.translateAndAliasParameters
import org.jetbrains.kotlin.py.translate.expression.translateFunction
import org.jetbrains.kotlin.py.translate.expression.wrapWithInlineMetadata
import org.jetbrains.kotlin.py.translate.general.TranslatorVisitor
import org.jetbrains.kotlin.py.translate.utils.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtensionProperty

abstract class AbstractDeclarationVisitor : TranslatorVisitor<Unit>()  {
    override fun emptyResult(context: TranslationContext) { }

    open val enumInitializerName: JsName?
        get() = null

    override fun visitClassOrObject(classOrObject: KtClassOrObject, context: TranslationContext) {
        ClassTranslator.translate(classOrObject, context, enumInitializerName)
        val descriptor = BindingUtils.getClassDescriptor(context.bindingContext(), classOrObject)
        context.export(descriptor)
    }

    override fun visitProperty(expression: KtProperty, context: TranslationContext) {
        val descriptor = BindingUtils.getPropertyDescriptor(context.bindingContext(), expression)
        if (descriptor.modality === Modality.ABSTRACT) return

        val propertyContext = context.newDeclaration(descriptor)

        val defaultTranslator = DefaultPropertyTranslator(descriptor, context, getBackingFieldReference(descriptor))
        val getter = descriptor.getter!!
        val getterExpr = if (expression.hasCustomGetter()) {
            translateFunction(getter, expression.getter!!, propertyContext).first
        }
        else {
            val function = context.getFunctionObject(getter)
            function.source = expression
            defaultTranslator.generateDefaultGetterFunction(getter, function)
            function
        }

        val setterExpr = if (descriptor.isVar) {
            val setter = descriptor.setter!!
            if (expression.hasCustomSetter()) {
                translateFunction(setter, expression.setter!!, propertyContext).first
            }
            else {
                val function = context.getFunctionObject(setter)
                function.source = expression
                defaultTranslator.generateDefaultSetterFunction(setter, function)
                function
            }
        }
        else {
            null
        }

        if (TranslationUtils.shouldAccessViaFunctions(descriptor) || descriptor.isExtensionProperty) {
            addFunction(descriptor.getter!!, getterExpr, expression.getter ?: expression)
            descriptor.setter?.let { addFunction(it, setterExpr!!, expression.setter ?: expression) }
        }
        else {
            addProperty(descriptor, getterExpr, setterExpr)
        }
    }

    override fun visitNamedFunction(expression: KtNamedFunction, context: TranslationContext) {
        val descriptor = BindingUtils.getFunctionDescriptor(context.bindingContext(), expression)
        val functionAndContext = if (descriptor.modality != Modality.ABSTRACT) {
            translateFunction(descriptor, expression, context)
        }
        else {
            null
        }

        addFunction(descriptor, functionAndContext?.first, expression)
    }

    override fun visitTypeAlias(typeAlias: KtTypeAlias, data: TranslationContext?) {}

    private fun translateFunction(
            descriptor: FunctionDescriptor,
            expression: KtDeclarationWithBody,
            context: TranslationContext
    ): Pair<JsExpression, TranslationContext> {
        val function = context.getFunctionObject(descriptor)
        function.source = expression
        function.body.source = expression.finalElement as? LeafPsiElement
        val innerContext = context.newDeclaration(descriptor).translateAndAliasParameters(descriptor, function.parameters)

        if (descriptor.isSuspend) {
            function.fillCoroutineMetadata(innerContext, descriptor, hasController = false)
        }

        if (!descriptor.isOverridable) {
            function.body.statements += FunctionBodyTranslator.setDefaultValueForArguments(descriptor, innerContext)
        }
        innerContext.translateFunction(expression, function)

        return Pair(innerContext.wrapWithInlineMetadata(context, function, descriptor), innerContext)
    }

    // used from kotlinx.serialization
    abstract fun addFunction(
            descriptor: FunctionDescriptor,
            expression: JsExpression?,
            psi: KtElement?
    )

    // used from kotlinx.serialization
    abstract fun addProperty(
            descriptor: PropertyDescriptor,
            getter: JsExpression,
            setter: JsExpression?
    )

    // used from kotlinx.serialization
    abstract fun getBackingFieldReference(descriptor: PropertyDescriptor): JsExpression
}