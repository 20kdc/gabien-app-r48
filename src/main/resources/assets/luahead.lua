-- This is released into the public domain.
-- No warranty is provided, implied or otherwise.

-- Header for "call local Lua" implementation.
-- The idea here is that if Lua is installed on the system, then R48 will run a self-test.
-- If this succeeds, then it considers the Lua installation "valid" and allows the user to write & run Lua programs via their Lua runtime.
-- This integration avoids any licensing, Java version, or other issues while giving power users a simple way to use scripting to achieve their goals.
-- File structure:
-- <R48 Current Directory - NOT ROOT FOLDER>/templuah.r48 :: This file
-- <R48 Current Directory - NOT ROOT FOLDER>/templuac.r48 :: Code (copied from "<script name>.lua")
-- <R48 Current Directory - NOT ROOT FOLDER>/templuat.r48 :: Object (input)
-- <R48 Current Directory - NOT ROOT FOLDER>/templuao.r48 :: Object (output)
-- These are all deleted on completion.
-- Notably, to prevent accidental nonsense, the object is loaded & saved as DAG by both sides.
-- Also note that the RubyIO stuff here is a restricted subset of actual Ruby Marshal capabilities,
--  specifically a DAG-enabled LS-mode R48ObjectBackend.
-- No string transform is applied.

local function readU8(file)
    return file:read(1):byte()
end
local function readU16LE(file)
    local l = readU8(file)
    local b = readU8(file)
    return l + (b * 256)
end
local function readU32LE(file)
    local l = readU16LE(file)
    local b = readU16LE(file)
    return l + (b * 65536)
end
local function readBlock(file, l)
    local t = file:read(l)
    if t:len() ~= l then error("Out of file") end
    return t
end

local function writeU8(file, v)
    file:write(string.char(v))
end
local function writeU16LE(file, v)
    v = math.floor(v) % 65536
    local l = v % 256
    local b = math.floor(v / 256)
    writeU8(file, l)
    writeU8(file, b)
end
local function writeU24LE(file, v)
    v = math.floor(v) % 0x1000000
    local l = v % 65536
    local b = math.floor(v / 65536)
    writeU16LE(file, l)
    writeU8(file, b)
end
local function writeU32LE(file, v)
    v = math.floor(v) % 0x100000000
    local l = v % 65536
    local b = math.floor(v / 65536)
    writeU16LE(file, l)
    writeU16LE(file, b)
end
-- This doesn't have to perform well at all, it's for communication with R48 only
local function readVI(file)
    local mode = readU8(file)
    if mode == 0xFC then
        return readU32LE(file)
    end
    if mode == 0x04 then
        return readU32LE(file)
    end
    error("Unsupported mode, you should only feed LS-mode R48ObjectBackend output into this")
end
local function writeVI(file, v)
    if (v >= 0) then
        writeU8(file, 4)
        writeU32LE(file, v)
    else
        writeU8(file, -4)
        writeU32LE(file, v)
    end
end
-- Used for hash reading, should be reliable in most cases
function rubyObjEquals(a, b)
    for k, v in pairs(a) do
        if b[k] ~= v then return false end
    end
    for k, v in pairs(b) do
        if a[k] ~= v then return false end
    end
    return true
end
function rubyHashGet(hashRIO, key)
    for k, v in pairs(hashRIO.hash) do
        if rubyObjEquals(key, k) then
            return v
        end
    end
end
local function loadRubyValue(file, symbolTable, gensym)
    local object = {}
    object.type = string.char(readU8(file))
    local handlingInstVars = false
    if object.type == "I" then
        handlingInstVars = true
        object.type = string.char(readU8(file))
    end
    if object.type == "o" then
        object.text = loadRubyValue(file, symbolTable, gensym).text
        handlingInstVars = true
    elseif (object.type == "{") or (object.type == "}") then
        object.hash = {}
        local vars = readVI(file)
        for i = 1, vars do
            local k = loadRubyValue(file, symbolTable, gensym)
            local v = loadRubyValue(file, symbolTable, gensym)
            object.hash[k] = v
        end
        if object.type == "}" then
            object.hashDefault = loadRubyValue(file, symbolTable, gensym)
        end
    elseif object.type == "[" then
        -- NOTE: The array is indexed from 0 because otherwise some stuff doesn't make sense.
        object.array = {}
        for i = 0, readVI(file) - 1 do
            object.array[i] = loadRubyValue(file, symbolTable, gensym)
        end
    elseif object.type == ":" then
        local tl = readVI(file)
        local text = readBlock(file, tl)
        gensym(text)
        object.text = text
    elseif object.type == ";" then
        object.type = ":"
        object.text = symbolTable[readVI(file)]
    elseif object.type == "i" then
        object.int = readVI(file)
    elseif (object.type == "\"") or (object.type == "f") then
        local tl = readVI(file)
        local text = readBlock(file, tl)
        object.text = text
    elseif (object.type == "u") then
        object.text = loadRubyValue(file, symbolTable, gensym).text
        local tl = readVI(file)
        local text = readBlock(file, tl)
        object.user = text
    elseif (object.type == "T") or (object.type == "F") or (object.type == "0") then
        -- nothing to do with these
    else
        error("Cannot load type " .. object.type)
    end
    if handlingInstVars then
        object.ivar = {}
        local vars = readVI(file)
        for i = 1, vars do
            local k = loadRubyValue(file, symbolTable, gensym)
            object.ivar[k.text] = loadRubyValue(file, symbolTable, gensym)
        end
    end
    return object
end
local function loadRuby(file)
    file = io.open(file, "rb")
    if readU8(file) ~= 4 then error("Bad magic[0]") end
    if readU8(file) ~= 8 then error("Bad magic[1]") end
    local st = {}
    local sti = 0
    local v = loadRubyValue(file, st, function (t)
        st[sti] = t
        sti = sti + 1
    end)
    file:close()
    return v
end
local function saveRubyValue(file, object)
    local handlingInstVars = false
    if object.ivar and (object.type ~= "o") then
        handlingInstVars = true
        file:write("I")
    end
    file:write(object.type)

    if object.type == "o" then
        saveRubyValue(file, {type = ":", text = object.text})
        handlingInstVars = true
    elseif (object.type == "{") or (object.type == "}") then
        local kl = {}
        for k, v in pairs(object.hash) do
            table.insert(kl, {k, v})
        end
        writeVI(file, #kl)
        for _, v in ipairs(kl) do
            saveRubyValue(file, v[1])
            saveRubyValue(file, v[2])
        end
        if object.type == "}" then
            saveRubyValue(file, object.hashDefault)
        end
    elseif object.type == "[" then
        -- NOTE: The array is indexed from 0 because otherwise some stuff doesn't make sense.
        local x = 0
        while object.array[x] do
            x = x + 1
        end
        writeVI(file, x)
        for i = 1, x do
            saveRubyValue(file, object.array[x - 1])
        end
    elseif object.type == "i" then
        writeVI(file, object.int)
    elseif (object.type == ":") or (object.type == "\"") or (object.type == "f") then
        -- out of order to merge symbol code
        writeVI(file, object.text:len())
        file:write(object.text)
    elseif (object.type == "u") then
        saveRubyValue(file, {type = ":", text = object.text})
        writeVI(file, object.user:len())
        file:write(object.user)
    elseif (object.type == "T") or (object.type == "F") or (object.type == "0") then
        -- nothing to do with these
    else
        error("Cannot save type " .. object.type)
    end
    if handlingInstVars then
        local wtbl = {}
        for k, v in pairs(object.ivar) do
            table.insert(wtbl, {k, v})
        end
        writeVI(file, #wtbl)
        for _, v in ipairs(wtbl) do
            saveRubyValue(file, {type = ":", text = v[1]})
            saveRubyValue(file, v[2])
        end
    end
end
local function saveRuby(file, object)
    file = io.open(file, "wb")
    file:write("\x04\x08")
    saveRubyValue(file, object)
    file:close()
end

function cloneRubyValue(v)
    local text = ""
    local fakeFileA = {
        write = function (ign, buf)
            text = text .. buf
        end
    }
    local fakeFileB = {
        read = function (ign, len)
            local block = text:sub(1, len)
            text = text:sub(len + 1)
            return block
        end
    }
    saveRubyValue(fakeFileA, v)
    local st = {}
    local sti = 0
    local v2 = loadRubyValue(fakeFileB, st, function (t)
        st[sti] = t
        sti = sti + 1
    end)
    return v2
end

local object = loadRuby("templuat.r48")
object = loadfile("templuac.r48")(object)
saveRuby("templuao.r48", object)