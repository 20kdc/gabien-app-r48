-- Commands to UI
-- Assistance for the EasyRPG Editor people.

-- This is released into the public domain.
-- No warranty is provided, implied or otherwise.

-- Good luck continuing from here. I personally am just going to stick with working on R48.
-- Me and Qt never quite got on well.
-- Output format:
-- First letter indicates line meaning.
-- '>': New file. Rest of the line is the command ID.
-- 'U': Unconditional. Rest of the line is the argument index (0 for text, 1+ is parameters)
--  Next line is the parameter type, determines which Qt class you have to deal with.
--  'string': QTextLine
--  'var_id': VariableRpgComboBox
--  'item_id': ItemRpgComboBox
--  'int': QSpinBox
--  Otherwise, it's an enumeration (NOT YET SUPPORTED FULLY, look in gen() and fix the code to make it output stuff
--  Note that enumerations ought to cause a call to "updateConds", which ought to in turn update all conditionals.
--  Enumerations are meant to be responsible for reasonably simple switching cases.
--  Next line again is the ID of the control.
-- 'C': Conditional. Rest of the line is the argument index.
--  Next line is the argument index of the selector (MUST be a numeric value, so this is 1+)
--  Then, 3-line groups (until a blank line on the first line, terminating it) of:
--   selector-value on a line (integer. this is the one which is blank to end.)
--   selector-type on a line (type, see unconditional types)
--   selector-control on a line (ID of the control)

local current_file
local current_conns = {}

local aliases = {}
local enums = {}
-- NOTE: Though these are not zero-based tables, the resulting indexes will be zero-based,
--  assuming everything is done correctly.
enums["int_boolean"] = { "False", "True" }

local function terminate_file()
    current_file:write("<item><widget class=\"QDialogButtonBox\" name=\"buttonBox\">\n")
    current_file:write("<property name=\"orientation\"><enum>Qt::Horizontal</enum></property>\n")
    current_file:write("<property name=\"standardButtons\"><set>QDialogButtonBox::Cancel|QDialogButtonBox::Ok</set></property>\n")
    current_file:write("</widget></item>\n")
    current_file:write("</layout></widget>\n")
    current_file:write("<customwidgets>\n")
    current_file:write(" <customwidget>\n")
    current_file:write("  <class>VariableRpgComboBox</class>\n")
    current_file:write("  <extends>QComboBox</extends>\n")
    current_file:write("  <header>src/tools/rpgcombobox.h</header>\n")
    current_file:write(" </customwidget>\n")
    current_file:write(" <customwidget>\n")
    current_file:write("  <class>ItemRpgComboBox</class>\n")
    current_file:write("  <extends>QComboBox</extends>\n")
    current_file:write("  <header>src/tools/rpgcombobox.h</header>\n")
    current_file:write(" </customwidget>\n")
    current_file:write("</customwidgets>\n")
    current_file:write("<resources/><connections>\n")
    for _, conn in ipairs(current_conns) do
        current_file:write("<connection><sender>" .. conn[1] .. "</sender><signal>" .. conn[2] .. "</signal>\n")
        current_file:write("<receiver>" .. conn[3] .. "</receiver><slot>" .. conn[4] .. "</slot></connection>\n")
    end
    current_conns = {}
    current_file:write("</connections><slots/></ui>\n")
    current_file:close()
end

local uniq_n = 0
local function uniq()
    uniq_n = uniq_n + 1
    return "uniq" .. uniq_n
end

local function enterframe(k)
    current_file:write("  <item>\n")
    current_file:write("   <widget class=\"QGroupBox\" name=\"" .. uniq() .. "\">\n")
    current_file:write("    <property name=\"title\">\n")
    current_file:write("     <string>" .. k .. "</string>\n")
    current_file:write("    </property>\n")
    current_file:write("    <layout class=\"QHBoxLayout\" name=\"" .. uniq() .. "\">\n")
end

local function leaveframe()
    current_file:write("    </layout>\n")
    current_file:write("   </widget>\n")
    current_file:write("  </item>\n")
end

local function gen(k, uid)
    if k == "string" then
        current_file:write("      <widget class=\"QLineEdit\" name=\"" .. uid .. "\">\n")
        current_file:write("       <property name=\"text\"><string>A giant sculpture of a red rose. This text appears if the QLineEdit isn't initialized properly.</string></property>\n")
        current_file:write("      </widget>\n")
    elseif k == "var_id" then
        current_file:write("      <widget class=\"VariableRpgComboBox\" name=\"" .. uid .. "\"/>\n")
    elseif k == "item_id" then
        current_file:write("      <widget class=\"ItemRpgComboBox\" name=\"" .. uid .. "\"/>\n")
    elseif k == "int" then
        current_file:write("      <widget class=\"QSpinBox\" name=\"" .. uid .. "\"/>\n")
    else
        -- Enum (This is responsible for causing updates, get it connected in!)
        current_file:write("      <widget class=\"QComboBox\" name=\"" .. uid .. "\">\n")
        for _, v in ipairs(enums[k]) do
        end
        table.insert(current_conns, { uid, "currentIndexChanged(int)", "Cmd_" .. cdef, "updateConds()" })
        current_file:write("</widget>\n")
    end
end

local idx = 0

while true do
    local l = io.read()
    if not l then terminate_file() return end
    local words = l:sub(2):gmatch("[^ ]+")
    local c = l:sub(1, 1)
    local cdef = l:match("^[0-9]+")
    if cdef then
        if current_file then terminate_file() end
        -- Begin command definition
        current_file = io.open("gen/cmd_" .. cdef .. ".ui", "w")
        current_file:write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        current_file:write("<ui version=\"4.0\"><class>Cmd_" .. cdef .. "</class>\n")
        current_file:write("<widget class=\"QDialog\" name=\"Cmd_" .. cdef .. "\">\n")
        current_file:write("<property name=\"windowModality\"><enum>Qt::WindowModal</enum></property>\n")
        current_file:write("<property name=\"geometry\"><rect><x>0</x><y>0</y><width>0</width><height>0</height></rect></property>\n")
        current_file:write("<property name=\"windowTitle\"><string>Cmd_" .. cdef .. "</string></property>\n")
        current_file:write("<property name=\"modal\"><bool>true</bool></property>\n")
        current_file:write("<layout class=\"QVBoxLayout\" name=\"verticalLayout\">\n")
        print(">" .. cdef)
        idx = 0
        table.insert(current_conns, { "buttonBox", "rejected()", "Cmd_" .. cdef, "reject()" })
        table.insert(current_conns, { "buttonBox", "accepted()", "Cmd_" .. cdef, "accept()" })
    else
        if c == "p" then
            local k = words()
            local v = words()
            if aliases[v] then v = aliases[v] end
            if k ~= "_" then
                print("U" .. idx) -- Unconditional.
                enterframe(k)
                current_file:write("     <item>\n")
                local ctrl = uniq()
                print(v)
                print(ctrl)
                gen(v, ctrl)
                current_file:write("     </item>\n")
                leaveframe()
            end
        end
        if c == "D" then
            -- Complicated one. Do not rely on default.
            local k = words()
            local v = words()
            print("C" .. idx) -- Conditional.
            idx = idx + 1
            print(v)
            words() -- default
            v = words()
            enterframe(k)
            while v do
                local v2 = words()
                --
                current_file:write("     <item>\n")
                local ctrl = uniq()
                print(v) -- enum val.
                if aliases[v2] then v2 = aliases[v2] end
                print(v2) -- resulting type
                print(ctrl)
                gen(k, ctrl)
                current_file:write("     </item>\n")
                --
                v = words()
            end
            print("")
            leaveframe()
        end
        if c == "d" then
            -- Description
            current_file:write("<item>\n")
            current_file:write(" <widget class=\"QLabel\">\n")
            current_file:write("  <property name=\"text\"><string>" .. l:sub(3) .. "</string></property>\n")
            current_file:write(" </widget>\n")
            current_file:write("</item>\n")
        end
    end
end
