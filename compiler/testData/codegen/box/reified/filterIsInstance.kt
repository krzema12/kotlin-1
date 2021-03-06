// TODO: muted for Python because it was muted for JS. Once Python doesn't piggy-back on JS, investigate if it can be re-enabled for Python.
// IGNORE_BACKEND: PYTHON
// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS

// WITH_RUNTIME

import kotlin.test.assertEquals

inline fun<reified T> Array<Any>.filterIsInstance(): List<T> {
    return this.filter { it is T }.map { it as T }
}

fun box(): String {
    val src: Array<Any> = arrayOf(1,2,3.toDouble(), "abc", "cde")

    assertEquals(arrayListOf(1,2), src.filterIsInstance<Int>())
    assertEquals(arrayListOf(3.0), src.filterIsInstance<Double>())
    assertEquals(arrayListOf("abc", "cde"), src.filterIsInstance<String>())
    assertEquals(src.toList(), src.filterIsInstance<Any>())

    return "OK"
}
