import json, math

TOKEN = "246250044d54e44ed93a3027440e9bf3"
WEST, EAST = 225, 270
Z_DL, Z_DR = -494, -492
Z_LL, Z_LR = -495, -491
Y_WATER = 63
BASE_Y = 65
MAX_ARCH = 5

def arch_y(x):
    t = (x - WEST) / (EAST - WEST)
    return BASE_Y + round(MAX_ARCH * math.sin(math.pi * t))

# Group X ranges by Y level
y_groups = {}
for x in range(WEST, EAST + 1):
    yd = arch_y(x)
    y_groups.setdefault(yd, []).append(x)

# Sort Y levels
y_levels = sorted(y_groups.keys())

commands = [
    # Clear everything first
    {"command": f"fill {WEST} {Y_WATER} {Z_LL} {EAST} {BASE_Y+MAX_ARCH+1} {Z_LR} air replace white_wool"}
]

# Build deck by Y level (each level is a contiguous range)
for yd, xs in y_groups.items():
    # Deck surface at this Y level
    fill_start = min(xs)
    fill_end = max(xs)
    commands.append({"command": f"fill {fill_start} {yd} {Z_DL} {fill_end} {yd} {Z_DR} white_wool"})
    
    # Fill underneath (from riverbed up to just below deck)  
    commands.append({"command": f"fill {fill_start} {Y_WATER} {Z_DL} {fill_end} {yd-1} {Z_DR} white_wool"})
    
    # Railings follow the arch
    y_rail = yd + 1  # railing top
    # Left railing
    commands.append({"command": f"fill {fill_start} {yd+1} {Z_LL} {fill_end} {y_rail} {Z_LL} white_wool"})
    # Right railing
    commands.append({"command": f"fill {fill_start} {yd+1} {Z_LR} {fill_end} {y_rail} {Z_LR} white_wool"})

# Calculate arch peak info
print(f"Arched bridge commands generated")
print(f"Bridge: X={WEST}-{EAST}, Z={Z_LL}-{Z_LR}")
print(f"Arch: Y={BASE_Y} at edges -> Y={BASE_Y+MAX_ARCH} at center")
print(f"Y levels: {y_levels}")
print(f"Total commands: {len(commands)}")

with open("C:\\Core\\IA-Local\\temp\\nsmod\\arch_bridge.json", "w") as f:
    json.dump(commands, f)
print("Saved to arch_bridge.json")
