# R48 Data Model

## History

So there's some history to go over here, as it'll cover various terms involved.

* Data Model 1 (DM1): Retroactive name for what became `IRIOGeneric` (formerly called `RubyIO`). This structure is basically necessary to load XP databases, which is what was implemented first. Not very good for memory use and not very good for load times on large databases.
* Data Model 2 (DM2): This was brought in to help with 2K/2K3. The Ikachan editor was the first testbed for the basic DM2 techniques, as shown by `IkaMap`'s description: `Data Model 2 proof of concept`.
	* This is when `IRIO` was brought in.
	* Basically, reflection is used to fill out fields. At first, this sounds less efficient (_reflection!?!?!?!_), but the structure used allows for further optimizations.
	* **On-demand data unpacking!**
		* This is the primary speed boost DM2 gained, though it's specific to 2K/2K3.
		* The core code behind this is in `DM2R2kObject`. The TLDR is that initialization gets disabled and instead any _access_ causes an unpack. Since the hashmap is always going to be created for any loaded object, it may as well be created for new objects. As such, they initialize to Lcf defaults.
* Data Model '2.5': `RORIO` and `DMKey`. Basically it became clear that there was a need for the Hash type to make some actual sense, so `DMKey` was made, which is basically an immutable comparable type with 'Rubyish enough to work' semantics. `RORIO` was brought in because `DMKey` and `IRIO` need a common interchangable base.
* Data Model 3 (DM3): In progress. Will release proper as part of R48 v2.0 '_Together, we will implement Undo_'. May not ship in my lifetime.

## Philosophy

**An IRIO is a smart pointer to some data.**

It may even be so smart that it looks like an entirely different kind of object. Alternatively, it may store the data within itself (and this is the common case).

`IRIOFixnum` and `IRIOGeneric` represent the common cases; an IRIO which stores some data of a fixed type and an IRIO which stores any data. _In these cases, the IRIO stores the data within itself._

For example, you may wrap an IRIO in another IRIO which makes it appear different for the purpose of effective editing.

_Beware that misuse of Schema may allow these wrapped IRIOs to stick around longer than is actually strictly valid. Be careful around letting the user hold **any** IRIO (even unwrapped) in a subwindow if it's possible for the field to get retyped without the subwindow noticing. Deleted is fine though, R48 doesn't care if you access 'deleted' data._

**RORIOs are const pointers.**

This is in-line with how IRIO extends RORIO (similar to how const pointers work in C++).

## Types

Data:

* `RORIO`: All data types are based off of this.
	* `DMKey`: Immutable `HashMap` key, etc.
	* `IRIO`: The universal abstract datatype of most of R48. _This is what bridges Schema and underlying data, and allows shared code between engines._
		* `IRIOData`: Contains `IRIO`-side change tracking logic (debounce) and forces savestates to be implemented.
			* `IRIOGeneric`: Generic mutable container for any data.
			* Engine-specific datatypes go somewhere in here, depending on what they are. In theory these types are private to the IO layer, outside of optimizations.
			* `IRIOTypedData`: Implements all the functions to throw, except for IVar functions and `getType`.
				* `IRIOFixedData`: Implements `getType` via final field.
					* `IRIOFixedObject`: This is the core object that 'in-field' data access is based around. As of the early DM3 work, `FixedObjectProps` stores 'scanned templates' via reflection. These templates contain factories for filling fields marked with the 'construction annotations' (`DMCX`) and information about fields marked with the 'binding annotations' `DMFXOBinding` and `DMOptional`.
						* `IRIOFixedObjectPacked`: Implements the core of on-demand unpack.
							* `DM2R2kObject`: Implements the R2K object IO logic. _Not all R2K structures subclass this, only ones that use the Lcf chunk container format._

Stuff that needs removing:

`IMagicalBinder`: Best described as 'just plain evil,' it was outdated as far back as _`IRIO`s._ (To put that into perspective, it has been outdated longer than it has existed.) It clings to life by legacy code like the PicPointerPatch handlers. It works, to this very day, by using the schema system to mirror changes forwards and backwards between a shadow copy of the target in a different format.

## Fixed Objects, automatic construction, etc.

`IRIOFixedObject` expects every IVar to be accessible via reflection on fields and constructed either by annotations (as a shorthand), a `public static Consumer<Subclass> exampleField_add = (v) -> v.exampleField = ...;` lambda, or a `public void exampleField_add() {` method. Using this, it automatically handles `getIVar`, `addIVar`, and `rmIVar`, while also constructing the contents of IVars for you (`initialize()` is a separate function to allow controlling this behaviour).

Beware: `IRIOFixedObjectPacked` gets particularly strict by denying overrides, because the code for unpacking is already kind of inherently messy and doing basically anything _before_ the unpack has run is a recipe for disaster. In particular, `initialize()` is now an empty final function. The entire basis of `IRIOFixedObjectPacked` is that an invalid state isn't invalid so long as it becomes valid before anyone notices.

Both pay very close attention to reflection in the following aspects:

* The annotations `DMFXOBinding` and `DMOptional` are of importance to the 'FXO binding layer' which maps instance variables.
	* The default implementations of the IVar manipulation functions use these annotations.
* There is also `addField`. This is conceptually `addIVar`, but it works using the Java reflection `Field`, and attempts to hunt for an implementation of `addIVar` using several strategies. (See `ReflectiveIRIOFactoryScanner`.)
	* If a second static field with the suffix `_add` exists (i.e. `myField_add`, this must be a `Consumer<WhateverTypeThisIsAnyway>` which receives the object to perform the `addIVar` on. This is a hopefully-somewhat-higher-performance alternative to the string comparison mechanism.
	* If an instance `void()` method with the same naming convention exists, that is called.
	* `DMCX` annotations specify a particular constructor call to make.

R2K unpacking uses `DM2LcfBinding`. Notably, a field does not need an FXO binding to be Lcf-bound, which is why `addField` exists as a distinct entity.

## Plans For DM3

_aka 'UNDO MUST HAPPEN'_

_aka R48 v2.0_

* It will be necessary to have a way for `IRIO`s to store a frozen state.
	* Frozen states _are shallow clones, not deep clones._ To me it seems best if frozen states are of the type `Runnable`, and running the frozen state restores it onto the `IRIO`.
		* These are shallow clones specifically _because_ it will preserve pointers to 'deleted' objects. Even if these objects are in an invalid state, whatever modifications made them invalid will be undone via more frozen states.
			* If it isn't done this way, the frozen state is only valid in isolation; it is not possible to apply further frozen states on sub-trees. (This is related to why the Revert button circa R48 v1.6 has to close all the Schema windows. _**All pointers become invalid.**_) In addition the memory usage and CPU requirements for storing complete clones for every modification are... prohibitive. Figuring out that this was what needed to be done is part of why a proposal for this took so long.
	* Undo/Redo will work by backing up frozen states of any object just before it is modified. Having explicit start/end brackets for modifications was considered, but ultimately I decided this was a bad idea. But, you say, _how will we know when an object is modified?_ The answer...
* `IDM3Context` needs to be properly used throughout R48. The goal here is that for any data-carrying `IRIO` in `ObjectDB`, _any(!)_ modification to that object's data should cause a notification to `IDM3Context`. It will then associate the current state of the `IRIO` to.
	* Freezable data is called `IDM3Data`. _Only `IDM3Data`s will carry the freeze/restore logic. All `IDM3Data`s will be required, one way or another, to report modifications to their context._
	* 'Wrapper' `IRIO`s must be silent about modifications. This is the `IRIO`/`IDM3Data` distinction. _They should still carry their context, it may prove useful later. If it's a problem then an additional class may be needed between IRIO and data-carrying objects._

## Requirements for `IDM3Data` implementors

The following guarantees MUST be followed:

1. Saving states of all `IDM3Data`s in a context at a given time will create a total snapshot of the data in that context.
2. In practice, snapshots will only contain those `IDM3Data` objects that reported modifications. This must work.
3. All involved `IRIO` objects that were valid at the time the state was taken must be valid and point to the semantically same objects upon reversion to that state.
(This specifically includes if an `IRIO` becomes invalid via object deletion, but then the deletion is reverted.)
4. IRIOs that were "theoretically accessible" via read-only operations when the state was taken must remain valid and point to the semantically same objects even if they didn't exist.
	* This is to cover an edge case with DM2 unpacking; the user may have dialogs open to objects that still semantically exist.
5. Performing a savestate, reverting to an older savestate, then going to the later savestate, must work properly. (Redo.)
6. For a collection of IDM3Data objects, it must not matter which order their states are restored in.
7. As long as all modified IDM3Data objects are restored to appropriate states, it must not be necessary to go "through" intermediate states.\
For a requirement of intermediate states to exist, that implies that the state is not a total snapshot of the IDM3Data.
8. The golden rule: Code outside the datamodel and undo/redo layer should see the data in the IRIOs 'magically' change to pre-modification, as if the code had been recording modifications and undoing them via the IRIO interface.

Practical considerations as a result of these rules:
1. The keyset of the latest save-point's map (`IDM3Data` to `Runnable`) is the exact inverse of the clean objects of the context's data.
2. Earlier save-points will likely be missing states from later save-points that need to also be reverted for consistency.
	These need to be managed somehow.
	What will need to be done here is entirely dependent on the undo/redo model.
	A key element is that you don't have a save-point for "right now", only "what the state was before the current batch of changes".
	To make a "redo savepoint", you take the current states (pre-undo) of everything you'll change by undoing.
3. To stop R48 from freeing an object with undo data (_THIS WOULD BE VERY, VERY BAD!!!_), use one context per loaded object.
	* Why is this useful or beneficial or wanted at all?\
	Well:
		* The context can store a reference to the `ILoadedObject`.
			* The reason the context is not the `ILoadedObject` itself is because the management of undo/redo is application-specific code and can't go in the IO library.
		* The `IRIO`s store references to the context.
		* The undo states store references to the `IRIO`s.
		* Therefore, as long as the undo states exist, objects that they refer to will remain loaded and thus guarantees are assured. As a nice bonus this means that loose `IRIO`s count as references which keep the object loaded.
		* In addition, this means that should the undo states be 'cropped' due to memory use concerns, this will cause object unloading.
