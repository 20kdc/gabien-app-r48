## MiniVM Details

(Also known as R48's Red Book, because colour-based book naming schemes are funny.)

*Note: Some classes described in this document overlap with D/MVM somewhat. The basic line of separation is that this document covers execution while D/MVM covers compilation and other 'outer shell' stuff.*

## Overview

MiniVM is a tree virtual machine. It avoids the usual pitfalls of AST virtual machines in the following ways:

* There's still a compilation pass. In theory, various languages *could* compile to MVM, but in practice there are two:
  
  * D/MVM -- this is the new main definition control language for R48, meant to clean up the inconsistent messes that are SDB and FormatSyntax.
  
  * PathSyntax -- this acted as an early test for MVM, and considering it a separate thing helps with all the existing uses of PathSyntax called directly from Java.

* Locals and globals are looked up during AST creation, rather than at runtime (Lua globals, most toy language variables)
  
  * Local references are stored as two-number addresses (stack frame, number).
  
  * Global references are stored as "slot" objects that directly hold the global value.
    
    * Due to this optimization, the core VM doesn't actually have to reference the environment at all, so further notes on this are in D/MVM's section. PathSyntax for example doesn't care.

* Lambdas with <= 4 arguments don't automatically allocate. (Not that they can't, just that it's possible to write code that doesn't allocate when run, and assuming the JVM optimizes value types passed as Object, it's possible to write *useful* code that doesn't allocate when run.)

There are some caveats:

* A hypothetical simplicity optimum would be to merge *global* and *local* access into one mechanism. However, this requires globals to live in an expandable array of some sort, and the access complexity added here isn't worth it.

```

```

## Data Types

The key assumption is that the JVM's optimization target has been towards ensuring that core boxed Java types such as `Double`, `Long`, and `String` are optimized.

Therefore, these sorts of types are used. An exception is that lists are represented as `List`. Arrays can't be resized.

If `Double` and `Long` in particular do cause unnecessary allocations, D/MVM might get revised a little to nudge them out of performance-critical paths, but the Android reference insists `Double.valueOf` is optimized (via some as of yet unknown method involving frequently used values). It is absurdly difficult (possibly impossible) to prove these claims, but they make them. For what it's worth, constant expressions keep the boxed object, so they won't *create* boxed objects per-run for no reason, and a custom `valueOf` might be worth attempting.

Notably, this is by no means an *exhaustive* list. The basic MVM types are those used by the Datum library: null, `Boolean`, `Long`, `Double`, `String`, `DatumSymbol`, and a List implementation: `LinkedList` or `ArrayList`. However, other types crop up simply whenever an object is fed into the system and expected to come out as an argument to some library routine.

In particular, `Character` exists (but maybe it shouldn't).

*All this said, it is advised that you don't use `Byte`, `Short`, or `Integer` for anything. It's just confusing.*

In practice, the base type of MiniVM is `java.lang.Object`.

## Core Details

The MVM core is principally made up of three elements: Expressions, scopes, and functions.

### Expressions

The most important element is MVMCExpr, a compiled MVM expression that runs within a runtime scope (MVMScope).

MVMCExpr has a single main `execute` function, taking an `MVMScope` and 8 *fast locals.* It also a pair of shortened `exc` versions -- when a MVMCExpr calls another MVMCExpr, `execute` is to be used, while the `exc` functions are for external calls.

During compilation, locals that cannot be implemented via *fast locals* are *deoptimized* into the MVMScope system. There are specific MVMCExprs which setup MVMScopes if necessary and continue with an expression within that scope.

Reasons for deoptimization include:

- The local is written to. Java doesn't have by-reference function arguments, so this doesn't work.

- The fast local slot was reused for something else, but then accessed inside that context.
  
  - This doesn't happen right now. See `newLocal` for reasoning.

- The fast local slot was overwritten, accessed from inside a constructed lambda).

The main purpose of fast locals is to support lambdas that *don't* allocate anything just for calling them. This is in contrast to most other interpreters written in Java.

## Scopes

The second most important element is MVMScope.

When setting up a new scope, the scope's *frame number* and *frame size* are defined. The scope's frame (an array of objects) is put into an array of arrays of objects, which inherits existing frames from the parent scope on creation.

There is a single immutable global static final root scope, `MVMScope.ROOT`, effectively a constant like `null` but without the downside of requiring specific checks. This scope, and only this scope (it wouldn't be a formal error to create other scopes with this property, but it would be wasteful), contains *no frames*.

Descendant scopes are created with their parent scope, a frame ID (the index in the array of all frames to which this scope introduces a frame), and the amount of locals within that frame. The parent scope isn't kept around, it's simply used as a source to copy the previous frames from.

Most importantly: It's a very regular occurrance for descendant scopes to skip over frames between them and their parent.

Therefore, take a case where a lambda has been created within another lambda, and the inner lambda requires a frame, but the outer lambda does not. This is a normal situation the MVM compiler will likely regularly encounter.

In this case, the outer lambda is Frame 0, and the inner lambda is Frame 1. When the outer lambda is called, the scope remains the root scope, and the inner lambda is bound *to that root scope*. The inner lambda, on execution, then creates a scope from this root scope with frame ID 1.

If the inner lambda needed to inherit any values from the outer lambda, the outer lambda would have had frame locals and a scope -- notably, the inner lambda does not need to have it's own scope to inherit values from the outer lambda, so a call to a lambda defined as `(lambda () x)` will never allocate memory outside of exceptional conditions (almost certainly literally exceptional). The lambda is bound to the parent scope, but does not define any locals that need a dedicated frame.

References to locals are stored as MVMCLocal, which contains the frame number and location within the frame of the local. A frame number of -1 indicates this is a fast-local.

Some rationale:

- The reason that globals are *not* built using this approach is mainly because more code may be compiled into an environment at any time. In this event attaching it to the existing scope system would require universal ArrayList use for type consistency. Meanwhile the existing scope system relies on a guarantee that the local sizes of any code being compiled as a single unit can be precisely measured before any code is executed (though not while it's being compiled, for compiler flexibility).

- Another interesting side effect is that environment lookups are potentially faster than locals. As all function calls are environment lookups, this is a good thing, and avoids the `local print = _ENV.print` problem.

- Of course, other less inconsistent approaches exist -- if you can require a function's environment reference never changes, you can auto-optimize the environment such that a slot approach is feasible while still making it fit the API of a table, and then apply similar logic to descendant environments with, i.e. a prototyping system -- but the requirements of fast locals (see later in this document) made these not reasonable courses of action.

### Functions

Functions, aka MVMFn, are where some of the other interesting GC performance tricks come into play.

Any function call with up to 4 parameters doesn't initially cause an allocation. An allocation may be caused if:

* The function was passed more than 4 parameters. (This immediately requires use of `callIndirect`, taking an `Object[]`)

* The function is user-defined and has a variable number of arguments (in which case a list must be created of those arguments).
  
  - MVMLambdaVAFn still attempts to avoid `callIndirect` when possible, which would need an intermediate array just to then put it into a list.

When `callIndirect` isn't involved, function calls are very simple -- a series of argument objects (and nothing else) are passed in, and a return value comes out the other side.
