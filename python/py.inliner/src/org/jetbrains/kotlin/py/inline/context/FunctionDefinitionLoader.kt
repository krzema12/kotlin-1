/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.py.inline.context

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.isCallableReference
import org.jetbrains.kotlin.js.backend.ast.metadata.descriptor
import org.jetbrains.kotlin.py.inline.*
import org.jetbrains.kotlin.py.inline.util.*
import org.jetbrains.kotlin.py.translate.context.Namer
import java.util.HashMap

class FunctionDefinitionLoader(
    private val inliner: JsInliner
) {
    fun getFunctionDefinition(call: JsInvocation, scope: InliningScope): InlineFunctionDefinition {
        return getFunctionDefinitionImpl(call, scope)!!
    }

    fun hasFunctionDefinition(call: JsInvocation, scope: InliningScope): Boolean {
        return getFunctionDefinitionImpl(call, scope) != null
    }

    val functionsByFunctionNodes = HashMap<JsFunction, FunctionWithWrapper>()

    /**
     * Gets function definition by invocation.
     *
     * Notes:
     *      1. Qualifier -- [()/.call()] part of invocation.
     *      2. Local functions are compiled like function literals,
     *      but called not directly, but through variable.
     *
     *      For example, local `fun f(a, b) = a + b; f(1, 2)` becomes `var f = _.foo.f$; f(1, 2)`
     *
     * Invocation properties:
     * 1. Ends with either [()/.call()].
     *
     * 2. Qualifier can be JsNameRef with static ref to JsFunction
     *    in case of function literal without closure.
     *
     *    For example, qualifier == _.foo.lambda$
     *
     * 3. Qualifier can be JsInvocation with static ref to JsFunction
     *    in case of function literal with closure. In this case
     *    qualifier arguments are captured in closure.
     *
     *    For example, qualifier == _.foo.lambda(captured_1)
     *
     * 4. Qualifier can be JsNameRef with static ref to case [2]
     *    in case of local function without closure.
     *
     * 5. Qualifier can be JsNameRef with ref to case [3]
     *    in case of local function with closure.
     */
    private fun getFunctionDefinitionImpl(call: JsInvocation, scope: InliningScope): InlineFunctionDefinition? {
        // Ensure we have the local function information
        assert(scope.fragment in fragmentInfo)

        return lookUpFunctionDirect(call, scope) ?: lookUpFunctionIndirect(call, scope) ?: lookUpFunctionExternal(call, scope.fragment)
    }

    private val functionReader = FunctionReader(inliner.reporter, inliner.config, inliner.bindingContext)

    private data class FragmentInfo(
        val functions: Map<JsName, FunctionWithWrapper>,
        val accessors: Map<String, FunctionWithWrapper>,
        val localAccessors: Map<CallableDescriptor, FunctionWithWrapper>
    )

    private fun JsProgramFragment.loadInfo(): FragmentInfo {
        return FragmentInfo(
            collectNamedFunctionsAndWrappers(listOf(this)),
            collectAccessors(listOf(this)),
            collectLocalFunctions(listOf(this))
        ).also { (functions, accessors) ->
            (functions.values.asSequence() + accessors.values.asSequence()).forEach { f ->
                functionsByFunctionNodes[f.function] = f
            }
        }
    }

    private val fragmentInfo = inliner.translationResult.newFragments.associateTo(mutableMapOf()) { it to it.loadInfo() }

    private fun lookUpStaticFunction(functionName: JsName?, fragment: JsProgramFragment): FunctionWithWrapper? =
        fragmentInfo[fragment]?.run { functions[functionName] }

    private fun lookUpStaticFunctionByTag(functionTag: String, fragment: JsProgramFragment): FunctionWithWrapper? =
        fragmentInfo[fragment]?.run { accessors[functionTag] }


    private fun lookUpFunctionIndirect(call: JsInvocation, scope: InliningScope): InlineFunctionDefinition? {
        /** Try loading via `descriptor` metadata.
         *  In case of private inline properties that's the only way... */
        call.descriptor?.let { descriptor ->
            fragmentInfo[scope.fragment]?.let { info ->
                info.localAccessors[descriptor]?.let { fn ->
                    return InlineFunctionDefinition(fn, null).also { def ->
                        // Definition fragment is that same as call site fragment
                        inliner.process(def, call, scope.fragment, scope)
                    }
                }
            }
        }

        /** remove ending `()` */
        val callQualifier: JsExpression = if (isCallInvocation(call)) {
            (call.qualifier as JsNameRef).qualifier!!
        } else {
            call.qualifier
        }

        /** process cases 2, 3 */
        val qualifier = callQualifier.transitiveStaticRef
        return when (qualifier) {
            is JsInvocation -> {
                tryExtractCallableReference(qualifier) ?: getSimpleName(qualifier)?.let { simpleName ->
                    lookUpStaticFunction(simpleName, scope.fragment)?.let { if (isFunctionCreator(it.function)) it else null }
                }
            }
            is JsNameRef -> lookUpStaticFunction(qualifier.name, scope.fragment)

            // Since we could get functionWithWrapper as a simple function directly from staticRef (which always points on implementation)
            // we should check if we have a known wrapper for it
            is JsFunction -> functionsByFunctionNodes[qualifier] ?: FunctionWithWrapper(qualifier, null)
            else -> null
        }?.let {
            InlineFunctionDefinition(it, null).also { definition ->
                // Definition fragment is that same as call site fragment
                inliner.process(definition, call, scope.fragment, scope)
            }
        }
    }

    private fun fragmentByTag(tag: String): JsProgramFragment? {
        return inliner.translationResult.inlineFunctionTagMap[tag]?.let { unit ->
            inliner.translationResult.getTranslationResult(unit).fragment.also { fragment ->
                fragmentInfo.computeIfAbsent(fragment) {
                    fragment.loadInfo()
                }
            }
        }
    }

    private fun lookUpFunctionDirect(call: JsInvocation, callsiteScope: InliningScope): InlineFunctionDefinition? {
        val descriptor = call.descriptor ?: return null

        val tag = Namer.getFunctionTag(descriptor, inliner.config, inliner.bindingContext)

        val definitionFragment = fragmentByTag(tag) ?: return null

        val fn = lookUpStaticFunctionByTag(tag, definitionFragment) ?: return null

        val definition = InlineFunctionDefinition(fn, tag)

        // Make sure definition has it's own inline calls inlined.
        inliner.process(definition, call, definitionFragment, callsiteScope)

        return definition
    }


    private fun lookUpFunctionExternal(call: JsInvocation, fragment: JsProgramFragment): InlineFunctionDefinition? =
        call.descriptor?.let { descriptor ->
            functionReader[descriptor, fragment]?.let {
                InlineFunctionDefinition(it, Namer.getFunctionTag(descriptor, inliner.config, inliner.bindingContext))
            }
        }

    private fun tryExtractCallableReference(invocation: JsInvocation): FunctionWithWrapper? {
        if (invocation.isCallableReference) {
            val arg = invocation.arguments[1]
            if (arg is JsFunction) return FunctionWithWrapper(arg, null)
        }
        return null
    }
}
