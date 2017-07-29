/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import r48.RubyIO;

/**
 * Created on 29/07/17.
 */
public interface IMagicalBinder {
    RubyIO targetToBound(RubyIO target);
    boolean applyBoundToTarget(RubyIO bound, RubyIO target);
}
