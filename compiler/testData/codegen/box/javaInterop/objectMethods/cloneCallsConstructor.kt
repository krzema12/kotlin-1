// TODO: muted for Python because it was muted for JS. Once Python doesn't piggy-back on JS, investigate if it can be re-enabled for Python.
// IGNORE_BACKEND: PYTHON
// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

data class A(var x: Int) : Cloneable {
    public override fun clone(): A = A(x)
}

fun box(): String {
    val a = A(42)
    val b = a.clone()
    if (b != a) return "Fail equals"
    if (b === a) return "Fail identity"
    return "OK"
}
