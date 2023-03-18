## D/MVM Language Details

(Also known as R48's Green Book, because colour-based book naming schemes are funny.)

You are expected to have read `vm-details.md` first.

## Overview

D/MVM is essentially an attempt to balance familiarity with Scheme with the requirements of a project written in Java trying not to destroy its own performance but also trying to keep concise code and also support Android (making dynamic class generation... hazardous).

D/MVM code execution starts with an *environment,* aka `MVMEnv`. Environments contain `MVMSlot` instances representing globals.

`MVMEnv` has the method `evalObject`, which creates a top-level scope, compiles a value in that top-level scope, and then executes the resulting compiled code. Loading a file is achieved by calling `evalObject` for each Datum value in the file sequentially.

Caveats exist:

- Scheme makes the insistence that top-level *programs* are important, as opposed to individual top-level *statements,* making REPLs difficult for no good reason. In contrast, D/MVM *always* reads in code "as if" it was entered at a REPL. In particular, each individual root-level Datum value is compiled and executed separately. Semantics outside of that get more complex.
  
  - The way in which this actually shows itself is in regards to anything where a macro definition is compiled at the same time as code that uses said macro definition. It won't work because the macro definition hasn't been executed yet. On the plus side, see the later notes on referential transparency in macros.

- It seems Scheme implementations are supposed to be flexible about top-level definitions. (See [R6RS rationale](https://standards.scheme.org/official/r6rs-rationale.pdf), 8. Top-level programs) MiniVM's early lookup kind of defeats this at present.
  
  - Hypothetically, D/MVM could be revised to be more Scheme-like by simply *creating* globals whenever it can't find one, but in practice even major Scheme implementations such as Guile issue warnings when you play games like this.
  
  - D/MVM takes it as a de-facto standard that you aren't supposed to refer to variables that haven't been bound in some fashion yet, and rather than issuing a warning, outright issues a compile error (due to R48 project priorities).

Example of GNU Guile issuing a warning for an unbound variable:

```
scheme@(guile-user)> (define (abc) baa) (define baa 123)
;;; <stdin>:1:14: warning: possibly unbound variable `baa'
scheme@(guile-user)> (abc)
$1 = 123
```

## Compilation

This defines, and follows some simple rules.

- If the expression is a symbol, a read of the variable referred to by that symbol is compiled. (See `readLookup` below.)
  
  - If the variable doesn't exist, that's a compile error.

- If the expression is a list:
  
  - If the list is empty, this is a compile error.
  
  - The first object in the list is compiled. The compiled expression is `ol1v`.
  
  - If `ol1v` is a *slot* or *constant expression,* the value inside that slot (*at compile-time*) or constant expression is checked to see if it is a macro.
    
    - If it's a macro, it's passed the uncompiled other args and the scope, and whatever that macro returns is the compiled result. User defined macros are wrapped in such a way as to implement a rough clone of S9FES semantics. See Macros section.
  
  - Failing any of these, a function call is compiled. *Notably, the function call doesn't care if `ol1v` contains an applyable as a source-level constant, or as an identifier reference, etc.*
    
    - At some point, it may become a compile-time error to make a function call to a constant function where that function's argument count is incompatible. Such a change wouldn't affect programs that don't already have bugs.
    
    - Another thing that could be done in this regard is to allow the creation of constant definitions, which, while occupying the namespace of slots, would be immutable and valid for such a compile-time checking scheme. This *would* affect programs that override library functions, but improves the reliability of the system.

- If the expression is *anything else,* it's turned into a constant expression. This includes procedures and macros, which is useful for hygenic macros.

## Compilation Details

Compilation occurs in a given *compile scope*. These have potentially unique implementations of the following operations:

- compileDefine: Given a symbol and a lambda returning a compiled expression, compiles effectively the `define` operation. The lambda is called within the run of `compileDefine`, but after the symbol has definitely been defined.
  
  - The local can't be a fast local. Implementing via a "auto-letrec" sort of deal was considered, but the problem here is that the required alterations go all the way up through compiler-level. This is likely why Scheme standards insist on defines being placed at the start of a body, however:
    
    - That rule essentially elevates them to an actual compilation rule rather than a macro with some special privileges.
    
    - Scheme standards also insist you implement `call/cc`, which requires reifying *the stack,* so even in Scheme standards it's a moot point -- unless you're assuming a hyper-aggressive future compiler that simply *knows* some procedures won't ever call `call/cc`, and has presumably exiled `eval` from the language, and has presumably made a guarantee that some procedure globals are constants and presumably just plain doesn't *have* a REPL.
      
      - The last time this rule was really consistent was in R4RS, as that was the last point the kinds of guarantees required to make use of the head-of-body `define` rule were possible -- since then, the best I can say for it is that it allows some implementations to treat `define` as something of a `letrec*`. Only, R4RS doesn't actually require an implementation even support head-of-body `define`. It takes head-of-body `define` more as a descriptivist "some implementations do this".
        
        - Suffice to say, D/MVM implements `define` at any location, because needless list depth is just annoying, as you can presumably tell from this entry.

- extendWithFrame: Creates a new scope with a formal frame boundary, like that between a lambda and its caller. Accessing parent locals deoptimizes them.

- readLookup: Given a symbol, compiles the expression to retrieve that symbol's value.

- writeLookup: Given a symbol and a compiled value expression, compiles effectively the `set!` operation.

- newLocal: Only supported in MVMSubScope (which has to be explicitly created). This introduces a new local mid-scope, returning the internal object controlling it.
  
  - Lambdas use this to setup their (fast-local unless there's >8 of them) args. The process for calling into a lambda orchestrates loading the locals.
  
  - `let` also uses this. Same basic idea, try to fast-local when possible.
  
  - Compiler must be careful not to "revive" any local erased through shadowing, so as a rule only use the MVMCLocal instance returned to compile a single write to and nothing else. When done in groups, handle in the order of creation. Unfortunately, the complexities of implementing various particular scoping requirements make enforcing this basically impossible.

The actual operation of compiling an expression occurs on a given MVMCompileScope, starting with an MVMToplevelScope, in the `compile` method.

## Macros

Macros and special forms in D/MVM are completely equivalent, and R4RS and later's "standard transformer environment" is avoided.

At the Java-level, macros and special forms are both MVMMacro, which, given the compile scope and the macro arguments, returns an MVMCExpr. That's it.

The basis of how the *user* defines macros comes from Scheme 9 From Empty Space, but the actual lookup and execution is described above in Compilation.

In particular:

- Of the syntax definition forms, only `define-syntax` is implemented in D/MVM, and the syntax is similar to S9FES, including supporting the lambda-define syntax. However, unlike S9FES, that the content must be a procedure is checked immediately. The lack of support for `let-syntax` and so forth is based on that it's wholly unnecessary unless the plan is to have macros generating and returning other macros.
  
  - A quick explanation of how this works: The procedure is essentially wrapped into a macro. Assuming `(define-syntax a a-lambda)`, then `(a 1 2 moose)` runs `(a-lambda 1 2 'moose)`. The returned value is expected to be source, which is then compiled.

- `(gensym)` exists. This is another aspect from S9FES, and it's the obvious workaround to the obvious issue of what to do without the complexity of the `syntax` type and so forth. It would be wrong to call this elegant, but as D/MVM doesn't use interned symbols, there's no risk of a symbol leak, and since the source tree of most gensym output isn't stored, the symbols will be quietly deleted.
  
  - If `gabien-datum` and D/MVM are changed to use interned symbols, then symbols will be a reference type rather than a boxed type. In this case, there is no reason not to have anonymous symbols, which `gensym` can safely return. There remains no risk of a symbol leak, although the rule that `(eq? (string->symbol (symbol->string x)) x)` will not be true for these cases.

- Everything inside a top-level `begin` occurs in the same compile/execute cycle. That is, `begin` has no inherent special behaviour for top-level scopes -- it simply passes on the scope it receives, but it still counts as one compilation/execute unit at top-level.
  
  - This is deliberate ignorance of the standards... and S9FES also does this, and it avoids implementing special support for what is essentially an edge case caused by the shift in focus to compilation. Proof: `(begin (define-syntax (bleh) (list + 1 1)) (bleh))`

Rationale:

- If Guile and S9FES are any indication, few care about the "standard transformer environment". Guile cares about R4RS's low-level macro system, but apparently other statements in the same submission to the REPL count as the "standard transformer environment". S9FES doesn't even try to comply with R4RS, opting instead to use the `define-syntax` name but effectively implement a mixture of R1RS macro semantics (page 16/17) with modern `define`-with-lambda syntax. It is this syntax that D/MVM uses.

- The following experiment with Guile shouldn't work according to R4RS, because the lambda here, while expanded in the REPL's environment, should be being *evaluated* in the "standard transformer environment".

```
(define somethingelseinstead (syntax 123))
(define-syntax llm (lambda (x) somethingelseinstead))
llm ; 123
```

- S9FES's mechanism is *minimalist yet powerful,* which as a guiding principle for D/MVM development seems like a good basis. The code `(define-syntax (adda a . b) (append (list + a) b))` followed by `(adda 1 2 3)` works on both D/MVM and S9FES.

- The problem described in R1RS note `{FUNCALL is a Pain}` (page 26/27) should be heeded. The "magic" status of a macro definition can exceed the ability of the environment to contain it.
  
  - Guile fixes this by storing macros in the environment, but using a different execution more in line with the standards.
  
  - S9FES seems to store macros *elsewhere,* thus invoking the problem R1RS warns of, but because it only appears to execute macros in the place of procedure calls, the impact is likely only limited to cases where a lambda is stored in a variable named after a macro, i.e. `(define-syntax (mu) 1) (let ((mu 2)) (mu))`
  
  - As previously stated, MVM stores macros in the environment.

## Important Macros

The following macros are essentially paired to internal functionality built just for them in the compile scope, due to being important for lexical scoping and so forth. Other macros are relatively tame, at most defining new expression types.

* `define` relies on a dedicated `compileDefine` function to differentiate top-level and otherwise scopes. Also, see `lambda` for those forms.
  
  * `define-syntax` reuses most of `define`.

* `lambda` relies on `extendWithFrame` and `newLocal` to setup the lambda's scope.

* `set!` uses `writeLookup`.

* `let` uses `extendMayFrame` and `newLocal`.
