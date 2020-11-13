
        package generated.Python
        
        sealed class stmt()
        
        
        data class FunctionDef(val name: identifier, val args: arguments, val body: kotlin.collections.List<stmt>, val decorator_list: kotlin.collections.List<expr>, val returns: expr?, val type_comment: string?) 
            : stmt() 
        

        data class AsyncFunctionDef(val name: identifier, val args: arguments, val body: kotlin.collections.List<stmt>, val decorator_list: kotlin.collections.List<expr>, val returns: expr?, val type_comment: string?) 
            : stmt() 
        

        data class ClassDef(val name: identifier, val bases: kotlin.collections.List<expr>, val keywords: kotlin.collections.List<keyword>, val body: kotlin.collections.List<stmt>, val decorator_list: kotlin.collections.List<expr>) 
            : stmt() 
        

        data class Return(val value: expr?) 
            : stmt() 
        

        data class Delete(val targets: kotlin.collections.List<expr>) 
            : stmt() 
        

        data class Assign(val targets: kotlin.collections.List<expr>, val value: expr, val type_comment: string?) 
            : stmt() 
        

        data class AugAssign(val target: expr, val op: operator, val value: expr) 
            : stmt() 
        

        data class AnnAssign(val target: expr, val annotation: expr, val value: expr?, val simple: int) 
            : stmt() 
        

        data class For(val target: expr, val iter: expr, val body: kotlin.collections.List<stmt>, val orelse: kotlin.collections.List<stmt>, val type_comment: string?) 
            : stmt() 
        

        data class AsyncFor(val target: expr, val iter: expr, val body: kotlin.collections.List<stmt>, val orelse: kotlin.collections.List<stmt>, val type_comment: string?) 
            : stmt() 
        

        data class While(val test: expr, val body: kotlin.collections.List<stmt>, val orelse: kotlin.collections.List<stmt>) 
            : stmt() 
        

        data class If(val test: expr, val body: kotlin.collections.List<stmt>, val orelse: kotlin.collections.List<stmt>) 
            : stmt() 
        

        data class With(val items: kotlin.collections.List<withitem>, val body: kotlin.collections.List<stmt>, val type_comment: string?) 
            : stmt() 
        

        data class AsyncWith(val items: kotlin.collections.List<withitem>, val body: kotlin.collections.List<stmt>, val type_comment: string?) 
            : stmt() 
        

        data class Raise(val exc: expr?, val cause: expr?) 
            : stmt() 
        

        data class Try(val body: kotlin.collections.List<stmt>, val handlers: kotlin.collections.List<excepthandler>, val orelse: kotlin.collections.List<stmt>, val finalbody: kotlin.collections.List<stmt>) 
            : stmt() 
        

        data class Assert(val test: expr, val msg: expr?) 
            : stmt() 
        

        data class Import(val names: kotlin.collections.List<alias>) 
            : stmt() 
        

        data class ImportFrom(val module: identifier?, val names: kotlin.collections.List<alias>, val level: int?) 
            : stmt() 
        

        data class Global(val names: kotlin.collections.List<identifier>) 
            : stmt() 
        

        data class Nonlocal(val names: kotlin.collections.List<identifier>) 
            : stmt() 
        

        data class Expr(val value: expr) 
            : stmt() 
        

            object Pass : stmt() 
        

            object Break : stmt() 
        

            object Continue : stmt() 
        
    