// TODO: muted for Python because it was muted for JS. Once Python doesn't piggy-back on JS, investigate if it can be re-enabled for Python.
// IGNORE_BACKEND: PYTHON
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_REFLECT

import kotlin.test.*

fun Int.foo(s: String) {}

class A {
    fun bar() {}
}

fun baz(name: String) {}

fun box(): String {
    assertEquals(
            listOf("extension receiver parameter of ${Int::foo}", "parameter #1 s of ${Int::foo}"),
            Int::foo.parameters.map(Any::toString)
    )

    assertEquals(
            listOf("instance parameter of ${A::bar}"),
            A::bar.parameters.map(Any::toString)
    )

    assertEquals(
            listOf("parameter #0 name of ${::baz}"),
            ::baz.parameters.map(Any::toString)
    )

    return "OK"
}
