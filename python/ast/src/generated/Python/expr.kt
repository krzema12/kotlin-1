
        package generated.Python
        
        sealed class expr()
        
        
        data class BoolOp(val op: boolop, val values: kotlin.collections.List<expr>) 
            : expr() 
        

        data class NamedExpr(val target: expr, val value: expr) 
            : expr() 
        

        data class BinOp(val left: expr, val op: operator, val right: expr) 
            : expr() 
        

        data class UnaryOp(val op: unaryop, val operand: expr) 
            : expr() 
        

        data class Lambda(val args: arguments, val body: expr) 
            : expr() 
        

        data class IfExp(val test: expr, val body: expr, val orelse: expr) 
            : expr() 
        

        data class Dict(val keys: kotlin.collections.List<expr>, val values: kotlin.collections.List<expr>) 
            : expr() 
        

        data class Set(val elts: kotlin.collections.List<expr>) 
            : expr() 
        

        data class ListComp(val elt: expr, val generators: kotlin.collections.List<comprehension>) 
            : expr() 
        

        data class SetComp(val elt: expr, val generators: kotlin.collections.List<comprehension>) 
            : expr() 
        

        data class DictComp(val key: expr, val value: expr, val generators: kotlin.collections.List<comprehension>) 
            : expr() 
        

        data class GeneratorExp(val elt: expr, val generators: kotlin.collections.List<comprehension>) 
            : expr() 
        

        data class Await(val value: expr) 
            : expr() 
        

        data class Yield(val value: expr?) 
            : expr() 
        

        data class YieldFrom(val value: expr) 
            : expr() 
        

        data class Compare(val left: expr, val ops: kotlin.collections.List<cmpop>, val comparators: kotlin.collections.List<expr>) 
            : expr() 
        

        data class Call(val func: expr, val args: kotlin.collections.List<expr>, val keywords: kotlin.collections.List<keyword>) 
            : expr() 
        

        data class FormattedValue(val value: expr, val conversion: int?, val format_spec: expr?) 
            : expr() 
        

        data class JoinedStr(val values: kotlin.collections.List<expr>) 
            : expr() 
        

        data class Constant(val value: constant, val kind: string?) 
            : expr() 
        

        data class Attribute(val value: expr, val attr: identifier, val ctx: expr_context) 
            : expr() 
        

        data class Subscript(val value: expr, val slice: expr, val ctx: expr_context) 
            : expr() 
        

        data class Starred(val value: expr, val ctx: expr_context) 
            : expr() 
        

        data class Name(val id: identifier, val ctx: expr_context) 
            : expr() 
        

        data class List(val elts: kotlin.collections.List<expr>, val ctx: expr_context) 
            : expr() 
        

        data class Tuple(val elts: kotlin.collections.List<expr>, val ctx: expr_context) 
            : expr() 
        

        data class Slice(val lower: expr?, val upper: expr?, val step: expr?) 
            : expr() 
        
    