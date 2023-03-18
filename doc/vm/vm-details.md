## D/MVM Virtual Machine Details

(Also known as the Red Book, because colour-based book naming schemes are funny.)

## VM Overview

MiniVM is a tree virtual machine. It avoids the usual pitfalls of AST virtual machines in the following ways:

* There's still a compilation pass. In theory, various languages *could* compile to MVM, but in practice there are two:
  
  * D/MVM -- this is the new main definition control language for R48, meant to clean up the inconsistent messes that are SDB and FormatSyntax.
  
  * PathSyntax -- this acted as an early test for MVM, and considering it a separate thing helps with all the existing uses of PathSyntax called directly from Java.

* Locals and globals are looked up during AST creation, rather than at runtime (Lua globals, most toy language variables)
  
  * Local references are stored as two-number addresses (stack frame, number).
  
  * Global references are stored as "slot" objects that directly hold the global value.

* Lambdas with <= 4 arguments don't automatically allocate. (Not that they can't, just that it's possible to write code that doesn't allocate after compilation, and assuming the JVM optimizes value types passed around by generics, it's possible to write *useful* code that doesn't allocate after compilation.)

There are some caveats:

* A hypothetical simplicity optimum would be to merge *global* and *local* access into one mechanism. However, this requires globals to live in an expandable array of some sort, and the access complexity added here isn't worth it.

* Scheme makes the insistence that top-level *programs* are important, as opposed to individual top-level *statements,* making REPLs difficult for no good reason. In contrast, D/MVM *always* reads in code "as if" it was entered at a REPL. In particular, each individual root-level Datum value is compiled and executed separately. Semantics outside of that get more complex.
  
  * The way in which this actually shows itself is in regards to anything where a macro definition is compiled at the same time as code that uses said macro definition. It won't work because the macro definition hasn't been executed yet. On the plus side, see the later notes on referential transparency in macros.

* It seems Scheme implementations are supposed to be flexible about top-level definitions. (See [R6RS rationale](https://standards.scheme.org/official/r6rs-rationale.pdf), 8. Top-level programs) MiniVM's early lookup kind of defeats this at present.
  
  * Hypothetically, MiniVM could be revised to be more Scheme-like by simply *creating* globals whenever it can't find one, but in practice even major Scheme implementations such as Guile issue warnings when you play games like this.
  
  * MiniVM takes it as a de-facto standard that you aren't supposed to refer to variables that haven't been bound in some fashion yet, and rather than issuing a warning, outright issues a compile error (due to R48 project priorities).

```
scheme@(guile-user)> (define (abc) baa) (define baa 123)
;;; <stdin>:1:14: warning: possibly unbound variable `baa'
scheme@(guile-user)> (abc)
$1 = 123
```

## VM Core Details

The MVM core is principally made up of two elements.

The most important element is MVMCExpr, a compiled MVM expression that runs within a runtime scope (MVMScope).

The second most important element is MVMScope. MVMScopes act as containers for local variables. There is a default empty MVMScope, which is `MVMScope.ROOT` -- lambdas which don't need reified locals use this scope.

MVMCExpr has a single main `execute` function, taking an `MVMScope` and 8 *fast locals.* It also a pair of shortened `exc` versions -- when a MVMCExpr calls another MVMCExpr, `execute` is to be used, while the `exc` functions are for external calls.

During compilation, locals that cannot be implemented via *fast locals* are *deoptimized* into the MVMScope system. There are specific MVMCExprs which setup MVMScopes if necessary and continue with an expression within that scope.

Reasons for deoptimization include:

* The local is written to. Java doesn't have by-reference function arguments, so this doesn't work.

* The fast local slot was reused for something else, but then accessed inside that context.
  
  * This doesn't happen right now. See `newLocal` for reasoning.

* The fast local slot was overwritten, accessed from inside a constructed lambda).

The main purpose of fast locals is to support lambdas that *don't* allocate anything just for calling them. This is in contrast to most other interpreters written in Java.

## Data Types

The key assumption is that the JVM's optimization target has been towards ensuring that core boxed Java types such as Double, Long, and String are optimized.

Therefore, these sorts of types are used. An exception is that lists are represented as List. Arrays can't be resized.

If Double and Long in particular do cause unnecessary allocations, D/MVM might get revised a little to nudge them out of performance-critical paths, but the Android reference insists `Double.valueOf` is optimized (via some as of yet unknown method involving frequently used values). It is absurdly difficult (possibly impossible) to prove these claims, but they make them. For what it's worth, constant expressions keep the boxed object, so they won't *create* boxed objects per-run for no reason, and a custom `valueOf` might be worth attempting.

## D/MVM

### Compilation

Compilation occurs in a given *compile scope*. These have potentially unique implementations of the following operations:

* compileDefine: Given a symbol and a lambda returning a compiled expression, compiles effectively the `define` operation. The lambda is called within the run of `compileDefine`, but after the symbol has definitely been defined.
  
  * The local can't be a fast local. Implementing via a "auto-letrec" sort of deal was considered, but the problem here is that the required alterations go all the way up through compiler-level. This is likely why Scheme standards insist on defines being placed at the start of a body, however:
    
    * That rule essentially elevates them to an actual compilation rule rather than a macro with some special privileges.
    
    * Scheme standards also insist you implement `call/cc`, which requires reifying *the stack,* so even in Scheme standards it's a moot point -- unless you're assuming a hyper-aggressive future compiler that simply *knows* some procedures won't ever call `call/cc`, and has presumably exiled `eval` from the language, and has presumably made a guarantee that some procedure globals are constants and presumably just plain doesn't *have* a REPL.
      
      * The last time this rule was really consistent was in $R^4RS$, as that was the last point the kinds of guarantees required to make use of the head-of-body `define` rule were possible -- since then, the best I can say for it is that it allows some implementations to treat `define` as something of a `letrec*`. Only, $R^4RS$ doesn't actually require an implementation even support head-of-body `define`. It takes head-of-body `define` more as a descriptivist "some implementations do this".
        
        * Suffice to say, D/MVM implements `define` at any location, because needless list depth is just annoying, as you can presumably tell from this entry.

* extendWithFrame: Creates a new scope with a formal frame boundary, like that between a lambda and its caller. Accessing parent locals deoptimizes them.

* readLookup: Given a symbol, compiles the expression to retrieve that symbol's value.

* writeLookup: Given a symbol and a compiled value expression, compiles effectively the `set!` operation.

* newLocal: Only supported in MVMSubScope (which has to be explicitly created). This introduces a new local mid-scope, returning the internal object controlling it.
  
  * Lambdas use this to setup their (fast-local unless there's >8 of them) args. The process for calling into a lambda orchestrates loading the locals.
  
  * This can't deallocate existing locals, because if it hits a local that's *not* already behind an FV barrier, it would have to forcefully deoptimize it even if it's never used here. The good news is that since lambdas don't inherit their parent's fast-locals, there's not really usually a reason to need to deallocate existing locals. Fast-locals are a short-lambda optimization and they aren't really in *that* short of a supply anyway.
  
  * `let` hasn't been implemented yet. Lambdas had to deal with similar problems, though, so there's a known pattern for how to implement it.
    
    * Compile the expressions in the parent scope, create a sub-scope without deoptimization barriers, and use a dedicated MVM expression to atomically execute the compiled expressions in the parent, create a frame if necessary, copy the fast-locals, and execute the expressions, writing into the (inner) locals from the expressions run using the (outer) locals.
      
      * The reason to ensure the expressions are run using the outer locals is in case newLocal gets replaced with something like `letrec`, in which case it's maybe possible that some of the inner fast-locals may be overwritten half-way through, and because the expressions are compiled in the outer scope, this won't cause a deoptimization (which would be annoying, anyway). At the same time, though, the inner fast-locals must default to the values of the outer fast-locals, because some outer fast-locals may survive.
    
    * Unfortunately, code reuse is not possible due the highly variadic yet GC-optimized nature of everything involved.

The actual operation of compiling an expression occurs on a given MVMCompileScope, starting with an MVMToplevelScope, in the `compile` method.

The rules are simple:

* If the expression is a symbol, `readLookup` compiles it.

* If the expression is a list:
  
  * If the list is empty, this is a compile error.
  
  * The first object in the list is compiled. The compiled expression is `ol1v`.
  
  * If `ol1v` is a *slot* or *constant expression,* the value inside that slot (*at compile-time*) or constant expression is checked to see if it is a macro.
    
    * If it's a macro, it's passed the uncompiled other args and the scope, and whatever that macro returns is the compiled result. User defined macros are wrapped in such a way as to implement a rough clone of S9FES semantics. See Macros section. 
  
  * Failing any of these, a function call is compiled. *Notably, the function call doesn't care if `ol1v` contains an applyable as a source-level constant, or as an identifier reference, etc.*
    
    * At some point, it may become a compile-time error to make a function call to a constant function where that function's argument count is incompatible. Such a change wouldn't affect programs that don't already have bugs.
    
    * Another thing that could be done in this regard is to allow the creation of constant definitions, which, while occupying the namespace of slots, would be immutable and valid for such a compile-time checking scheme. This *would* affect programs that override library functions, but improves the reliability of the system.

* If the expression is *anything else,* it's turned into a constant expression. This includes procedures and macros, which is useful for hygenic macros.

### Macros

Macros and special forms in D/MVM are completely equivalent, and $R^4RS$ and later's "standard transformer environment" is avoided.

At the Java-level, macros and special forms are both MVMMacro, which, given the compile scope and the macro arguments, returns an MVMCExpr. That's it.

The basis of how the *user* defines macros comes from Scheme 9 From Empty Space, but the actual lookup and execution is described above in Compilation.

In particular:

* Of the syntax definition forms, only `define-syntax` is implemented in D/MVM, and the syntax is similar to S9FES, including supporting the lambda-define syntax. However, unlike S9FES, that the content must be a procedure is checked immediately. The lack of support for `let-syntax` and so forth is based on that it's wholly unnecessary unless the plan is to have macros generating and returning other macros.
  
  * A quick explanation of how this works: The procedure is essentially wrapped into a macro. Assuming `(define-syntax a a-lambda)`, then `(a 1 2 moose)` runs `(a-lambda 1 2 'moose)`. The returned value is expected to be source, which is then compiled.

* `(gensym)` exists. This is another aspect from S9FES, and it's the obvious workaround to the obvious issue of what to do without the complexity of the `syntax` type and so forth. It would be wrong to call this elegant, but as D/MVM doesn't use interned symbols, there's no risk of a symbol leak, and since the source tree of most gensym output isn't stored, the symbols will be quietly deleted.
  
  * If `gabien-datum` and D/MVM are changed to use interned symbols, then symbols will be a reference type rather than a boxed type. In this case, there is no reason not to have anonymous symbols, which `gensym` can safely return. There remains no risk of a symbol leak, although the rule that `(eq? (string->symbol (symbol->string x)) x)` will not be true for these cases.

* Everything inside a top-level `begin` occurs in the same compile/execute cycle. That is, `begin` has no inherent special behaviour for top-level scopes -- it simply passes on the scope it receives, but it still counts as one compilation/execute unit at top-level.
  
  - This is deliberate ignorance of the standards... and S9FES also does this, and it avoids implementing special support for what is essentially an edge case caused by the shift in focus to compilation. Proof: `(begin (define-syntax (bleh) (list + 1 1)) (bleh))`

Rationale:

* If Guile and S9FES are any indication, few care about the "standard transformer environment". Guile cares about $R^4RS$'s low-level macro system, but apparently other statements in the same submission to the REPL count as the "standard transformer environment". S9FES doesn't even try to comply with $R^4RS$, opting instead to use the `define-syntax` name but effectively implement a mixture of $R^1RS$ macro semantics (page 16/17) with modern `define`-with-lambda syntax. It is this syntax that D/MVM uses.

* The following experiment with Guile shouldn't work according to $R^4RS$, because the lambda here, while expanded in the REPL's environment, should be being *evaluated* in the "standard transformer environment".

```
(define somethingelseinstead (syntax 123))
(define-syntax llm (lambda (x) somethingelseinstead))
llm ; 123
```

* S9FES's mechanism is *minimalist yet powerful,* which as a guiding principle for D/MVM development seems like a good basis. The code `(define-syntax (adda a . b) (append (list + a) b))` followed by `(adda 1 2 3)` works on both D/MVM and S9FES.

* The problem described in $R^1RS$ note `{FUNCALL is a Pain}` (page 26/27) should be heeded. The "magic" status of a macro definition can exceed the ability of the environment to contain it.
  
  * Guile fixes this by storing macros in the environment, but using a different execution more in line with the standards.
  
  * S9FES seems to store macros *elsewhere,* thus invoking the problem $R^1RS$ warns of, but because it only appears to execute macros in the place of procedure calls, the impact is likely only limited to cases where a lambda is stored in a variable named after a macro, i.e. `(define-syntax (mu) 1) (let ((mu 2)) (mu))`
  
  * As previously stated, MVM stores macros in the environment.

### Functions

Functions, aka MVMFn, are where some of the other interesting GC performance tricks come into play.

Any function call with up to 4 parameters doesn't initially cause an allocation. An allocation may be caused if:

* The function was passed more than 4 parameters. (This immediately requires use of `callIndirect`, taking an `Object[]`)

* The function is user-defined and has a variable number of arguments (in which case a list must be created of those arguments).
  
  - MVMLambdaVAFn still attempts to avoid `callIndirect` when possible, which would need an intermediate array just to then put it into a list.
