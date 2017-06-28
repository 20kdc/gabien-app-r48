/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import r48.schema.SchemaElement;

/**
 * All schema elements which, data-wise, solely wrap another element, should implement this.
 * Created on 6/28/17.
 */
public interface IProxySchemaElement {
    SchemaElement getEntry();
}
