--
-- This is released into the public domain.
-- No warranty is provided, implied or otherwise.
--

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
 else
  for k, v in ipairs(ls(f)) do
   check(f .. "/" .. v)
  end
 end
end
check("src/main/java")
