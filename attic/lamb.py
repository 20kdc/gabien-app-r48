# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

# This program is kept here just in case someone might need to grab it at some point.
# 󱥁󱤎󱤧󱤈󱤬󱥧󱥫󱤖󱤡󱤑󱤧󱤘󱥷󱥁

# 󱥁󱤎󱤧󱦅󱤉󱤪󱤄
# 󱥁󱤧󱤆󱥁󱤪:
# "app.ts("󱤧󱥶
# "T.z.l1234"󱤧󱤖

# P.S. I'm practicing toki pona. o kalama ala.

import os

# find ../app/src/main/java -type f > kulupu_lipu.txt
kulupu_lipu = open("kulupu_lipu.txt", "r")
lipu_pi_toki_inli = open("toki_inli.txt", "w")

sitelen_lon = {}
nanpa_sitelen_lon = 0

def alasa(sitelen):
	global sitelen_lon, nanpa_sitelen_lon, lipu_pi_toki_inli
	if sitelen in sitelen_lon:
		return sitelen_lon[sitelen]
	nimi = "l" + str(nanpa_sitelen_lon)
	sitelen_lon[sitelen] = nimi
	nanpa_sitelen_lon += 1
	lipu_pi_toki_inli.write("\t" + nimi + " \"" + sitelen + "\"\n")
	return nimi

def ante(sitelen_lipu):
	while True:
		# 󱤃󱤉󱤬󱤆
		lon_ante = sitelen_lipu.find("app.ts(\"")
		if lon_ante == -1:
			break
		lon_pini_ante = sitelen_lipu.find("\")", lon_ante + 8)
		# 󱥈󱤉󱥠
		sitelen_pini = sitelen_lipu[:lon_ante]
		sitelen_lon = sitelen_lipu[lon_ante + 8:lon_pini_ante]
		sitelen_kama = sitelen_lipu[lon_pini_ante + 2:]
		# 󱤟󱤉󱥳󱥈
		sitelen_lipu = sitelen_pini + "T.z." + alasa(sitelen_lon) + sitelen_kama
	return sitelen_lipu

while True:
	lipu_nimi = kulupu_lipu.readline()
	if lipu_nimi == "":
		break
	lipu_nimi = lipu_nimi.strip()
	print(lipu_nimi)
	# 󱤮󱤉󱤪
	lipu = open(lipu_nimi, "r")
	sitelen_lipu = lipu.read()
	lipu.close()
	# 󱤆󱤉󱤪
	sitelen_lipu = ante(sitelen_lipu)
	# 󱥠󱤉󱤪
	lipu = open(lipu_nimi, "w")
	lipu.write(sitelen_lipu)
	lipu.close()

