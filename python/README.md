# Python backend

The goal is to have Kotlin-Python interop, starting with being able to compile Kotlin to Python.

See discussion at https://discuss.kotlinlang.org/t/idea-python-backend/19852

## Progress

- [ ] Kotlin-Python interop
  - [ ] compile Kotlin to Python
    - [x] describe Python AST with Kotlin
      - [x] create a generic tool that converts [ASDL](https://www.usenix.org/legacy/publications/library/proceedings/dsl97/full_papers/wang/wang.pdf) to Kotlin entities
        - [x] create a parser of ASDL
        - [x] create a generator of Kotlin entities from parsed ASDL
        - [x] try to represent an example Python program, and fix any issues found
      - [x] convert [Python's grammar in ASDL](https://github.com/python/cpython/blob/master/Parser/Python.asdl) into AST entities
    - [ ] translate Kotlin IR to Python AST
      - [x] translate IR items present in the simple Python AST example (assignment, list, for loop, function invocation...)
      - [ ] move backend (Python IR -> AST mapping) logic to a separate module (don't piggy-back on JS stuff, like lowerings)
      - [ ] move frontend to a separate module (don't piggy-back on JS stuff)
      - [x] make a simple `fun hello() = "hello"` function translated and executable by Python (rest of the bundle at least parses fine by Python)
      - [x] set up box tests infra
      - [ ] translate all IR items
      - [ ] make all existing box tests pass
      - [ ] clean up before committing to master (if happens)
      - ...
    - [ ] generate Python code from Python AST
      - [x] AST items used in the simple example (unblocking end-to-end PoC)
      - [ ] all Python AST items
  - [ ] use Python from Kotlin
    - ...

## Running box tests

```
./gradlew :python:box.tests:pythonTest --tests "org.jetbrains.kotlin.python.test.ir.semantics.IrPythonCodegenBoxTestGenerated" | tee tests-out.txt
less tests-out.txt | grep "FAILED" | grep "Codegen" | sort > python/experiments/failed-tests.txt
```

Tips and tricks to speed up tests:

```shell script
# create ramdisk where temporary test py files are generated extensively (about 15% boost):
sudo mount -t tmpfs -o size=10G myramdisk python/py.translator/testData

mount | tail -n 1  # check it's mounted successfully
sudo umount python/py.translator/testData  # unmount
```

Also, set the number of `maxParallelForks` for `projectTest` in `buildSrc/src/main/kotlin/tasks.kt` to a high number like `CPU_THREADS-1`. This can give a boost too, for example, it gives another 15% boost on 12-thread CPU.

### Test stats

Current status: ![coverage](http://www.yarntomato.com/percentbarmaker/button.php?barPosition=20&leftFill=) (**1051**/5368 passed)

### History (newest on top)

* after removing empty variable declaration: **1051**/5368 passed (+3)

* after supporting comparison: **1048**/5368 passed (+6)

* after supporting some constructors, some inheritance...: **1042**/5368 passed (+75)

* after adding support for class methods: **967**/5368 passed (+28)

* with Python interpreter in place, no other changes to the compiler: **939**/5368 passed (+84)

* with no Python interpreter hooked into the tests and
  * when `OK` returned from the interpreter interface: all passed
  * when something else returned from the interpreter interface: **855**/5368 passed
    (the ones that succeeded are `testAllFilesPresentIn...` tests which doesn't depend on the actual invocation of code under test)
