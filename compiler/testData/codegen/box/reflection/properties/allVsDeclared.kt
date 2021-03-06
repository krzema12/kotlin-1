// TODO: muted for Python because it was muted for JS. Once Python doesn't piggy-back on JS, investigate if it can be re-enabled for Python.
// IGNORE_BACKEND: PYTHON
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_REFLECT

import kotlin.reflect.full.*
import kotlin.test.*

open class Super {
    val a: Int = 1
    val String.b: String get() = this
}

class Sub : Super() {
    val c: Double = 1.0
    val Char.d: Char get() = this
}

fun box(): String {
    val sub = Sub::class

    assertEquals(listOf("a", "c"), sub.memberProperties.map { it.name }.sorted())
    assertEquals(listOf("b", "d"), sub.memberExtensionProperties.map { it.name }.sorted())
    assertEquals(listOf("c"), sub.declaredMemberProperties.map { it.name })
    assertEquals(listOf("d"), sub.declaredMemberExtensionProperties.map { it.name })

    return "OK"
}
