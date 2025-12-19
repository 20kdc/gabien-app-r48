# R48 SchemaPath Principles

**Before reading this document, please be sure you read the data model document so you know what an IRIO is.**

SchemaPaths in R48 are difficult to comprehend, and explaining it roughly amounts to explaining **_Schema as a system_** (as opposed to just a format).

The design criteria were as follows:

1. Make autocorrection work.
2. Allow mixing data-bound code and traditional code by providing a clear way to signal when something has happened to the data.
3. Throw this together in about two weeks.
	* If it seems like R48's data model is based on a version of Ruby from an alternate dimension with multiple dimensions and time travel, that's not an accident. Keeping undo/redo in the data model was the only way to avoid a full rewrite, and it took an awfully long time to figure out how to make it happen. This is also why undo/redo is 'frame-based'.

If it provides any comfort, it could have been worse. Early versions of R48 were concerned with referential integrity, until it was clear that this would never come up and Schema couldn't support it anyway because arbitrary data-defined aliasing can blindside SchemaPath with things happening in paths that don't even exist yet, nevermind would know to be notified.

The first thing to understand about SchemaPaths is the meaning of `buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path)`.

* `target` is the exact IRIO that this specific invocation of the schema element is accessing.
	* 
* `launcher` is used to perform actions that require a direct interface with the schema host.
	* Different schema hosts were mostly a theoretical for most of R48's development (since usually things were in a Schema window, it was almost always both possible and reasonable to reuse that window), but the concept suddenly became important because as it turns out it's actually useful to be able to specify i.e. a Switch ID using the Switch ID UI inside something like RMTools.
* `path` serves a dual purpose:
	* It is the schema path that needs to be notified if something is changed.
	* It contains _additional context,_ such as array indices, subwindows, etc.

The simplest way of understanding this is that there are two trees.

There is the _schema path_ tree and the _view tree._ The schema path tree contains many 'dummy' nodes which are essentially 'additional breadcrumbs', used by the _real_ (`editor` and `targetElement`) nodes.

![](schema-paths.drawio.svg)

_Do note that while this is based on the idea of views, `modifyVal` and `visit` use the same split-IRIO format._

`SchemaPath.newWindow` is responsible for creating 'real' path nodes (those with Editor and Target), apart from the root. By comparison, the subviews may use 'intermediate' schema paths or even the root path despite not editing the root IRIO.

At some point, schema paths may be 'folded' somehow, perhaps their breadcrumbs being grouped with `target`. For now, the current, somewhat messy, system exists.

An additional help in understanding the situation is the existence of `visit`, which is essentially purified view-tree logic.