// TODO: muted for Python because it was muted for JS. Once Python doesn't piggy-back on JS, investigate if it can be re-enabled for Python.
// IGNORE_BACKEND: PYTHON
// !LANGUAGE: -ReleaseCoroutines
// IGNORE_BACKEND: JVM_IR
// IGNORE_BACKEND: JS
// WITH_RUNTIME
// WITH_COROUTINES
// DONT_TARGET_EXACT_BACKEND: JS_IR
// DONT_TARGET_EXACT_BACKEND: JS_IR_ES6

import helpers.*
import kotlin.coroutines.experimental.*

enum class Foo(vararg expected: String) {
    A("start", "A", "end"),
    B("start", "BCD", "end"),
    C("start", "BCD", "end"),
    D("start", "BCD", "end"),
    E("start", "E", "end"),
    F("start", "end");

    val expected = expected.toList()
}

fun box(): String {
    for (c in Foo.values()) {
        val actual = getSequence(c).toList()
        if (actual != c.expected) {
            return "FAIL: -- ${c.expected} != $actual"
        }
    }

    return "OK"
}

fun getSequence(a: Foo) =
    buildSequence {
        yield("start")
        when (a) {
            Foo.A -> {
                yield("A")
            }
            Foo.B,
            Foo.C,
            Foo.D-> {
                yield("BCD")
            }
            Foo.E-> {
                yield("E")
            }
        }
        yield("end")
    }
