# MVM Guide

The "Definitely not a Scheme VM", aka MiniVM, aka MVM, is being used for an increasing number of functions in R48.

This directory is divided into core concepts (this file) and libraries (other files here).

## Introduction

The MinIVM is an interpreted virtual machine layered on Java aiming for minimum GC waste and maximum flexibility.

Importantly, it does not use dynamic Java bytecode compilation, which avoids the need for dedicated Android support and some of the oddities with dynamic class-loading.

However, the main advantage over a runtime such as LuaJ is the reduction in allocations to a bare minimum. (LuaJ, for example, must create a new stack with every user function call. MiniVM allocations are mainly restricted to base, hopefully VM-optimized types such as String and the number types.)

On top of MiniVM is a Scheme-like language based on the Datum S-expression language (see `gabien-common/datum/specification.md` ).

This was chosen for the minimal syntax and macro support, owing to the various "convenience features" that had to continually be added to SDB.

## Values

The basic MiniVM types are those used by the Datum library: null, boolean, Long, Double, String, DatumSymbol, and LinkedList or ArrayList.

Additional types that may be seen at runtime are: The other Java Number sub-types, Character, Class, IRIO, and arrays (considered vectors).

Read-only or fixed-size list kinds are acceptable but may cause unusual behaviours in user code.

In practice, the base type of MiniVM is the Java Object.

## Basic Flow Of Execution

MiniVM code is made up of *functions* and *expressions*.

*Functions* make up the procedures of code, and are handled as tangible values, while *expressions* are the VM's equivalent to bytecode. The *expressions* of any user-defined code can be seen with `(mvm-disasm V)` where `V` is a lambda or expression object. *This transformation is not reversible and is not necessarily reliable -- there is no expression assembler, as this level of introspection isn't necessary.*

Any function call with up to 4 arguments can be entered without performing any GC allocation (The function may perform GC allocation regardless, however).

Both functions and expressions can be arbitrarily defined from Java code, but only macros or code using the VM for its own purposes (no point in reinventing the wheel while you already have a reinvented wheel) can make any use of new expressions.

Function instances are defined relative to *environments*, while expression calls operate within *scopes*.

Scopes are used for inheritance of locals (which have highly contextual references for which hash-map lookup is inefficient), while environments contain slots (similar to Clojure Atoms, but not at user level, and not actually guaranteed to be atomic).

At compile-time, references to slots are resolved *directly* by hash-map into the slot instances, so compile-time cost is relatively high but runtime cost is a field read/write (this appears to be why the Scheme system considers the top-level environment to be a dedicated concept in $R^4RS$, or a dedicated type in $R^5RS$).

References to locals are resolved to numeric IDs that can be reasonably efficiently accessed via an array of arrays of objects, which will be described very shortly.

But first, some rationale:

+ The reason that the environment is *not* built using this approach is mainly because more code may be compiled into an environment at any time. In this event attaching it to the existing scope system would require universal ArrayList use for type consistency. Meanwhile the existing scope system relies on a guarantee that the local sizes of any code being compiled as a single unit can be precisely measured before any code is executed.

+ Another interesting side effect is that environment lookups are potentially faster than locals. As all function calls are environment lookups, this is a good thing, and avoids the `local print = _ENV.print` problem.

+ Of course, other less inconsistent approaches exist -- if you can require a function's environment reference never changes, you can auto-optimize the environment such that a slot approach is feasible while still making it fit the API of a table, and then apply similar logic to descendant environments with, i.e. a prototyping system -- but the requirements of fast locals (see later in this document) made these not reasonable courses of action.

## Operation Of Scopes

A scope does *not* make direct reference to its parent scope in a "chaining" approach. Instead, all scopes contain an array mapping frame IDs to frames. Each frame is an array of objects.

If the array of objects would be of zero length, no frame is created, and a scope won't ever be created solely to house a frame that also does not exist. However, scopes may be created for frames that do exist descended from frames that don't exist. Frames that don't exist are left as null, and never read. Remember, these only occupy a few empty slots in small arrays being allocated anyway -- this is less wasteful than the alternatives, while still avoiding a two-pass compiler.

There is a single immutable global static final root scope, `MVMScope.ROOT`, effectively a constant like `null` but without the downside of requiring specific checks. This scope, and only this scope (it wouldn't be a formal error to create other scopes with this property, but it would be wasteful), contains *no frames*.

Descendant scopes are created with their parent scope, a frame ID (the index in the array of all frames to which this scope introduces a frame), and the amount of locals within that frame. The parent scope isn't kept around, it's simply used as a source to copy the previous frames from.

Most importantly: It's a very regular occurrance for descendant scopes to skip over frames between them and their parent.

Therefore, take a case where a lambda has been created within another lambda, and the inner lambda requires a frame, but the outer lambda does not. This is a normal situation the MVM compiler will likely regularly encounter.

In this case, the outer lambda is Frame 0, and the inner lambda is Frame 1. When the outer lambda is called, the scope remains the root scope, and the inner lambda is bound *to that root scope*. The inner lambda, on execution, then creates a scope from this root scope with frame ID 1.

If the inner lambda needed to inherit any values from the outer lambda, the outer lambda would have had frame locals and a scope -- notably, the inner lambda does not need to have it's own scope to inherit values from the outer lambda, so a call to a lambda defined as `(lambda () x)` will never allocate memory outside of exceptional conditions (almost certainly literally exceptional). The lambda is bound to the parent scope, but does not define any locals that need a dedicated frame.

## Fast Locals

Fast Locals are the optimization MVM provides over other options.

When calling an expression, 8 fast locals are provided. These are mostly equivalent in their function to immutable `let`, but cannot be passed into lambdas (so such cases will be de-optimized) and being immutable cannot be changed by `set!` or `define` (so such cases will be de-optimized).

*Importantly, the compiler assumes complete control over which locals are fast.*

Fast locals are considered to have a frame ID of -1. Allocating them an ID like this is useful, as the storage location of a local is mutable during compilation (this is what de-optimization refers to).

MVM does not de-optimize old locals after running out of fast locals, but fast locals that have been de-optimized at the time the local is being allocated count as free slots.

The main use of fast locals is for lambda arguments. By providing lambda arguments as fast locals, functions that are simply single expressions operating on their arguments directly do not need to allocate stack space.
