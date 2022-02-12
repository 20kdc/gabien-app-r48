-- gabien-app-r48 - Editing program for various formats
-- Written starting in 2016 by contributors (see CREDITS.txt)
-- To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
-- You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.


-- If the directory names are poisoned... just... WTF?
local function ls(dir)
 local t = {}
 local p = io.popen("ls " .. dir, "r")
 while true do
  local l = p:read()
  if not l then break end
  table.insert(t, l)
 end
 p:close()
 return t
end

local handled = {}
local function handle(str)
 if not handled[str] then
  handled[str] = true
  print("x " .. str)
 end
end

local function findtr(f, e)
 while true do
  local l = f:read()
  if not l then return end
  local matcher = l:gmatch("TXDB%.get%(\".-\"%)")
  while true do
   local v = matcher()
   if not v then break end
   if not handled[v] then
    handled[v] = true
    print("x " .. v:sub(10, v:len() - 1))
   end
  end
 end
end
local function check(f)
 print(" - " .. f .. " -")
 if f:sub(f:len() - 4, f:len()) == ".java" then
  local fd = io.open(f, "r")
  findtr(fd, f)
  fd:close()
 elseif f:sub(f:len() - 3, f:len()) == ".txt" then
  -- do nothing
 else
  for k, v in ipairs(ls(f)) do
   check(f .. "/" .. v)
  end
 end
end
check("app/src/main/java")
print(" - app/src/main/java/r48/FontSizes.java.fields - ")
-- FontSizes has extra-special logic
local f = io.open("app/src/main/java/r48/FontSizes.java", "r")
while true do
 local line = f:read()
 if not line then break end
 local field = line:match("public static int .*%;")
 if field then
  handle("\"" .. field:sub(19, -2) .. "\"")
 end
end
f:close()
