/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.directives

sealed class DirectivesContainer {
    object Empty : SimpleDirectivesContainer()

    abstract operator fun get(name: String): Directive?
    abstract operator fun contains(directive: Directive): Boolean
}

abstract class SimpleDirectivesContainer : DirectivesContainer() {
    private val registeredDirectives: MutableMap<String, Directive> = mutableMapOf()

    override operator fun get(name: String): Directive? = registeredDirectives[name]

    protected fun directive(name: String, description: String): SimpleDirective {
        return SimpleDirective(name, description).also(this::registerDirective)
    }

    protected fun stringDirective(name: String, description: String): StringValueDirective {
        return StringValueDirective(name, description).also(this::registerDirective)
    }

    protected inline fun <reified T : Enum<T>> enumDirective(
        name: String,
        description: String,
        noinline additionalParser: ((String) -> T?)? = null
    ): ValueDirective<T> {
        val possibleValues = enumValues<T>()
        val parser: (String) -> T? = { value -> possibleValues.firstOrNull { it.name == value } ?: additionalParser?.invoke(value) }
        return ValueDirective(name, description, parser).also(this::registerDirective)
    }

    protected fun <T : Any> valueDirective(name: String, description: String, parser: (String) -> T?): ValueDirective<T> {
        return ValueDirective(name, description, parser).also(this::registerDirective)
    }

    protected fun registerDirective(directive: Directive) {
        registeredDirectives[directive.name] = directive
    }

    override fun contains(directive: Directive): Boolean {
        return directive in registeredDirectives.values
    }

    override fun toString(): String {
        return buildString {
            appendLine("Directive container:")
            for (directive in registeredDirectives.values) {
                append("  ")
                appendLine(directive)
            }
        }
    }
}

class ComposedDirectivesContainer(private val containers: List<DirectivesContainer>) : DirectivesContainer() {
    constructor(vararg containers: DirectivesContainer) : this(containers.toList())

    override fun get(name: String): Directive? {
        for (container in containers) {
            container[name]?.let { return it }
        }
        return null
    }

    override fun contains(directive: Directive): Boolean {
        return containers.any { directive in it }
    }
}
