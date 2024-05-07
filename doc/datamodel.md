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
		- `IRIOGeneric`: Generic mutable container for any data.
		* Engine-specific datatypes such as `r48.io.r2k.obj.EventPage` go here (backed by, you know, a lot of extra Stuff). In theory these types are private to the IO layer, outside of optimizations.
	

Stuff that needs removing:

`IMagicalBinder`: Best described as 'just plain evil,' it was outdated as far back as _`IRIO`s._ (To put that into perspective, it has been outdated longer than it has existed.) It clings to life by legacy code like the PicPointerPatch handlers. It works, to this very day, by using the schema system to mirror changes forwards and backwards between a shadow copy of the target in a different format.

## Plans For DM3

_aka 'UNDO MUST HAPPEN'_

_aka R48 v2.0_

* It will be necessary to have a way for `IRIO`s to store a frozen state.
	* Frozen states _are shallow clones, not deep clones._ To me it seems best if frozen states are of the type `Runnable`, and running the frozen state restores it onto the `IRIO`.
		* These are shallow clones specifically _because_ it will preserve pointers to 'deleted' objects. Even if these objects are in an invalid state, whatever modifications made them invalid will be undone via more frozen states.
			* If it isn't done this way, the frozen state is only valid in isolation; it is not possible to apply further frozen states on sub-trees. (This is related to why the Revert button circa R48 v1.6 has to close all the Schema windows. _**All pointers become invalid.**_) In addition the memory usage and CPU requirements for storing complete clones for every modification are... prohibitive. Figuring out that this was what needed to be done is part of why a proposal for this took so long.
	* Undo/Redo will work by backing up frozen states of any object just before it is modified. Having explicit start/end brackets for modifications was considered, but ultimately I decided this was a bad idea. But, you say, _how will we know when an object is modified?_ The answer...
* `IDM3Context` needs to be properly used throughout R48. The goal here is that for any data-carrying `IRIO` in `ObjectDB`, _any(!)_ modification to that object's data should cause a notification to `IDM3Context`. It will then associate the current state of the `IRIO` to.
	* Due to various concerns, it feels best that these are called `IRIOData`. _Only `IRIOData`s will carry the freeze/restore logic. All `IRIOData`s will be required, one way or another, to report modifications to their context._
	* 'Wrapper' `IRIO`s must be silent about modifications. This will be enforced through the `IRIO`/`IRIOData` distinction. _They should still carry their context, it may prove useful later. If it's a problem then just push it down to `IRIOData`._