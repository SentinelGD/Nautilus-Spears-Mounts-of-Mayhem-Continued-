import json, sys, math

TOKEN = "246250044d54e44ed93a3027440e9bf3"
BASE = "http://127.0.0.1:26547"

# Bridge at Z=-493 (player position)
# River: X=233-269 (water), banks at X=225 and X=270

WEST = 225
EAST = 270
Z_CENTER = -493
WIDTH_L = -495  # left railing
WIDTH_DECK_L = -494
WIDTH_DECK_R = -492
WIDTH_R = -491  # right railing

Y_DECK = 65  # bridge surface
Y_WATER = 63  # river surface
Y_RAILING_TOP = 67

# Pillars every ~9 blocks
PILLAR_X = list(range(WEST, EAST + 1, 9))
if PILLAR_X[-1] != EAST:
    PILLAR_X.append(EAST)

# Phase 1: Build scaffold with white wool
commands = []

# Pillars (from riverbed to deck)
for x in PILLAR_X:
    cmd = f'fill {x} {Y_WATER} {WIDTH_DECK_L} {x} {Y_DECK} {WIDTH_DECK_L} white_wool'
    commands.append({"command": cmd})
    cmd = f'fill {x} {Y_WATER} {WIDTH_DECK_R} {x} {Y_DECK} {WIDTH_DECK_R} white_wool'
    commands.append({"command": cmd})

# Bridge deck
cmd = f'fill {WEST} {Y_DECK} {WIDTH_DECK_L} {EAST} {Y_DECK} {WIDTH_DECK_R} white_wool'
commands.append({"command": cmd})

# Railings (walls on both sides)
cmd = f'fill {WEST} {Y_DECK} {WIDTH_L} {EAST} {Y_RAILING_TOP} {WIDTH_L} white_wool'
commands.append({"command": cmd})
cmd = f'fill {WEST} {Y_DECK} {WIDTH_R} {EAST} {Y_RAILING_TOP} {WIDTH_R} white_wool'
commands.append({"command": cmd})

# Arch fill under deck (pillar-to-pillar arches using stairs)
# For simplicity, fill the underside with white wool too
cmd = f'fill {WEST} {Y_WATER} {WIDTH_DECK_L} {EAST} {Y_DECK-1} {WIDTH_DECK_R} white_wool'
commands.append({"command": cmd})

# Save commands
with open("C:\\Core\\IA-Local\\temp\\nsmod\\bridge_scaffold.json", "w") as f:
    json.dump(commands, f, indent=2)

print(f"Generated {len(commands)} commands for bridge scaffold")
print(f"Bridge: X={WEST} to {EAST}, Z={WIDTH_L} to {WIDTH_R}")
print(f"Pillars at: {PILLAR_X}")
