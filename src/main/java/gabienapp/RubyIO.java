/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * 'Dear [REDACTED].
 *  I have recently learned a valuable lesson about friendship.
 *  Specifically, that if you are friends with someone who uses Ruby Marshal
 *  as anything but a temporary serialization mechanism, and I emphasize TEMPORARY,
 *  maybe reconsidering your friendships is a good idea.'
 * Thankfully, I wasn't ever friends with [REDACTED] to begin with, since they're a company.
 * Not a person.
 * I pity the fool who downloads the first poisoned [NAME HERE] savefile.
 * Created on 12/27/16.
 */
public class RubyIO {
    public int type;
    public byte[] strVal; // actual meaning depends on iVars. Should be treated as immutable - replace strVal on change
    public String symVal;
    public HashMap<String, RubyIO> iVars = new HashMap<String, RubyIO>();
    public HashMap<RubyIO, RubyIO> hashVal;
    public RubyIO hashDefVal;
    public RubyIO[] arrVal;
    public byte[] userVal;
    public long fixnumVal;

    public RubyIO() {

    }

    public RubyIO setNull() {
        type = '0';
        strVal = null;
        symVal = null;
        iVars.clear();
        hashVal = null;
        hashDefVal = null;
        arrVal = null;
        userVal = null;
        fixnumVal = 0;
        return this;
    }

    public RubyIO setFX(long fx) {
        setNull();
        type = 'i';
        fixnumVal = fx;
        return this;
    }

    public RubyIO setShallowClone(RubyIO clone) {
        type = clone.type;
        strVal = clone.strVal;
        symVal = clone.symVal;
        iVars.clear();
        iVars.putAll(clone.iVars);
        if (clone.hashVal != null) {
            hashVal = new HashMap<RubyIO, RubyIO>();
            hashVal.putAll(clone.hashVal);
        } else {
            hashVal = null;
        }
        hashDefVal = clone.hashDefVal;
        if (clone.arrVal != null) {
            arrVal = new RubyIO[clone.arrVal.length];
            for (int i = 0; i < arrVal.length; i++)
                arrVal[i] = clone.arrVal[i];
        } else {
            arrVal = null;
        }
        if (clone.userVal != null) {
            userVal = new byte[clone.userVal.length];
            for (int i = 0; i < userVal.length; i++)
                userVal[i] = clone.userVal[i];
        } else {
            userVal = null;
        }
        fixnumVal = clone.fixnumVal;
        return this;
    }

    // That's deep, man. [/decadesIDidntLiveIn]
    public RubyIO setDeepClone(RubyIO clone) {
        setShallowClone(clone);
        for (Map.Entry<String, RubyIO> a : clone.iVars.entrySet())
            iVars.put(a.getKey(), new RubyIO().setDeepClone(a.getValue()));
        if (hashDefVal != null)
            hashDefVal = new RubyIO();
        if (hashVal != null)
            for (Map.Entry<RubyIO, RubyIO> a : clone.hashVal.entrySet())
                hashVal.put(new RubyIO().setDeepClone(a.getKey()), new RubyIO().setDeepClone(a.getValue()));
        if (arrVal != null)
            for (int i = 0; i < arrVal.length; i++)
                arrVal[i] = new RubyIO().setDeepClone(arrVal[i]);
        // userVal is actually copied over by the shallow clone
        return this;
    }

    public static boolean rubyTypeEquals(RubyIO a, RubyIO b) {
        if (a == b)
            return true;
        if (a.type != b.type)
            return false;
        if (a.type == 'o')
            return a.symVal.equals(b.symVal);
        if (a.type == 'u')
            return a.symVal.equals(b.symVal);
        return true;
    }

    // used to check Hash stuff
    public static boolean rubyEquals(RubyIO a, RubyIO b) {
        if (a == b)
            return true;
        if (a.type != b.type)
            return false;
        // primitive types
        if (a.type == 'i')
            return a.fixnumVal == b.fixnumVal;
        if (a.type == '\"')
            return a.decString().equals(b.decString());
        if (a.type == ':')
            return a.symVal.equals(b.symVal);
        if (a.type == 'T')
            return true;
        if (a.type == 'F')
            return true;
        if (a.type == '0')
            return true;
        return false;
    }

    @Override
    public String toString() {
        String data = "";
        if (type == 'u')
            return symVal + ";" +  userVal.length + "b";
        if (type == 'o')
            return symVal;
        if (type == '[')
            data = arrVal.length + "]";
        if (type == ':')
            data = symVal;
        if (type == '"')
            return "\"" + decString() + "\"";
        if (type == 'i')
            return Long.toString(fixnumVal);
        return ((char) type) + data;
    }

    public String decString() {
        // ignore the CP-setting madness for now
        // however, if it is to be implemented,
        // the specific details are that:
        // SOME (not all) strings, are tagged with an ":encoding" iVar.
        // This specifies their encoding.
        return new String(strVal, Charset.forName("UTF-8"));
    }
    public void encString(String text) {
        try {
            strVal = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public RubyIO getInstVarBySymbol(String cmd) {
        return iVars.get(cmd);
    }

    // NOTE: this is solely for cases where an external primitive is being thrown in.
    //       in most cases, we already have the RubyIO object by-ref.
    //       (Can't implement equals on RubyIO objects safely due to ObjectDB backreference tracing.)
    public RubyIO getHashVal(RubyIO rio) {
        for (Map.Entry<RubyIO, RubyIO> e : hashVal.entrySet())
            if (rubyEquals(e.getKey(), rio))
                return e.getValue();
        return null;
    }
}
