// TODO: muted for Python because it was muted for JS. Once Python doesn't piggy-back on JS, investigate if it can be re-enabled for Python.
// IGNORE_BACKEND: PYTHON
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_REFLECT

import kotlin.test.assertEquals

object Obj {
    fun foo() = 1
}

class A {
    companion object {
        fun foo() = 2
    }
}

class B {
    companion object Factory {
        fun foo() = 3
    }
}

class C

fun box(): String {
    assertEquals(1, Obj::class.objectInstance!!.foo())
    assertEquals(2, A.Companion::class.objectInstance!!.foo())
    assertEquals(3, B.Factory::class.objectInstance!!.foo())

    assertEquals(null, C::class.objectInstance)
    assertEquals(null, String::class.objectInstance)
    assertEquals(Unit, Unit::class.objectInstance)

    return "OK"
}
