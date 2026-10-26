"""Render the Morning Stretch icon from the Minecraft player model in the full stretch pose.

Usage:
  python3 tools/render_icon.py src/main/resources/assets/morning-stretch/icon.png
  python3 tools/render_icon.py docs/gallery/featured.png --width 1280 --height 720 --texel 14 --supersample 2
The pose matches StretchAnimations.compute at full stretch; requires Pillow.
"""
import argparse, math, random
from PIL import Image

parser = argparse.ArgumentParser()
parser.add_argument("output")
parser.add_argument("--width", type=int, default=128)
parser.add_argument("--height", type=int, default=128)
parser.add_argument("--texel", type=float, default=3.15, help="output pixels per model unit")
parser.add_argument("--supersample", type=int, default=4)
args = parser.parse_args()

SS = args.supersample
W = args.width * SS
H = args.height * SS
TEXEL = args.texel * SS     # screen pixels per model unit
rng = random.Random(11)

def shade(c, f):
    return tuple(max(0, min(255, round(v * f))) for v in c[:3])

def noisy(c, a=0.06):
    return shade(c, 1 + rng.uniform(-a, a))

def lerp(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))

# --- Steve-like textures (front/back/sides/top/bottom), indexed [row][col] ---
SKIN, SKIN_D, HAIR = (196, 145, 108), (160, 110, 80), (61, 41, 24)
WHITE, IRIS, MOUTH = (240, 240, 240), (78, 70, 160), (110, 66, 50)
SHIRT, PANTS, SHOES = (0, 170, 170), (62, 60, 165), (98, 98, 104)

def tex(w, h, fn):
    return [[noisy(fn(r, c)) for c in range(w)] for r in range(h)]

def head_front(r, c):
    if r <= 1 or (r == 2 and c in (0, 7)):
        return HAIR
    if r == 4 and c in (1, 6):
        return WHITE
    if r == 4 and c in (2, 5):
        return IRIS
    if r == 5 and c in (3, 4):
        return SKIN_D
    if r == 6 and 2 <= c <= 5:
        return MOUTH
    return SKIN

HEAD = {
    "front": tex(8, 8, head_front),
    "back": tex(8, 8, lambda r, c: HAIR if r <= 5 else SKIN),
    "side": tex(8, 8, lambda r, c: HAIR if r <= 2 or (r <= 4 and c >= 5) else SKIN),
    "top": tex(8, 8, lambda r, c: HAIR),
    "bottom": tex(8, 8, lambda r, c: SKIN_D),
}
BODY = {k: tex(8 if k in ("front", "back", "top", "bottom") else 4, 12 if k not in ("top", "bottom") else 4, lambda r, c: SHIRT)
        for k in ("front", "back", "side", "top", "bottom")}
ARM = {k: tex(4, 12 if k not in ("top", "bottom") else 4, (lambda r, c: SHIRT if r <= 3 else SKIN) if k not in ("top", "bottom") else (lambda r, c: SHIRT if k == "top" else SKIN))
       for k in ("front", "back", "side", "top", "bottom")}
LEG = {k: tex(4, 12 if k not in ("top", "bottom") else 4, (lambda r, c: PANTS if r <= 9 else SHOES) if k not in ("top", "bottom") else (lambda r, c: PANTS if k == "top" else SHOES))
       for k in ("front", "back", "side", "top", "bottom")}

# --- Model geometry (Minecraft units, y down, front faces -z) ---
def rot(p, xr, yr, zr):
    x, y, z = p
    # MC applies Z, then Y, then X (rotationZYX): v' = Rz * Ry * Rx * v
    y, z = y * math.cos(xr) - z * math.sin(xr), y * math.sin(xr) + z * math.cos(xr)
    x, z = x * math.cos(yr) + z * math.sin(yr), -x * math.sin(yr) + z * math.cos(yr)
    x, y = x * math.cos(zr) - y * math.sin(zr), x * math.sin(zr) + y * math.cos(zr)
    return (x, y, z)

def box_faces(origin, size, pivot, rotation, textures):
    ox, oy, oz = origin
    sx, sy, sz = size
    c = lambda x, y, z: tuple(a + b for a, b in zip(rot((x, y, z), *rotation), pivot))
    x0, x1, y0, y1, z0, z1 = ox, ox + sx, oy, oy + sy, oz, oz + sz
    # Each face: (p00, p10, p01) corners for texture (u right, v down), texture, outward normal (local).
    return [
        (c(x0, y0, z0), c(x1, y0, z0), c(x0, y1, z0), textures["front"], (0, 0, -1)),
        (c(x1, y0, z1), c(x0, y0, z1), c(x1, y1, z1), textures["back"], (0, 0, 1)),
        (c(x0, y0, z1), c(x0, y0, z0), c(x0, y1, z1), textures["side"], (-1, 0, 0)),
        (c(x1, y0, z0), c(x1, y0, z1), c(x1, y1, z0), textures["side"], (1, 0, 0)),
        (c(x0, y0, z1), c(x1, y0, z1), c(x0, y0, z0), textures["top"], (0, -1, 0)),
        (c(x0, y1, z0), c(x1, y1, z0), c(x0, y1, z1), textures["bottom"], (0, 1, 0)),
    ], rotation

# Full-stretch pose from StretchAnimations.compute at progress ~0.48 (rise = 1, yawn = 1).
SPREAD = math.radians(110)
parts = [
    box_faces((-4, -8, -4), (8, 8, 8), (0, 0, 0), (-0.32, 0, 0), HEAD),
    box_faces((-4, 0, -2), (8, 12, 4), (0, 0, 0), (-0.07, 0, 0), BODY),
    box_faces((-3, -2, -2), (4, 12, 4), (-5, 2, 0), (-0.12, 0.18, SPREAD), ARM),   # right arm
    box_faces((-1, -2, -2), (4, 12, 4), (5, 2, 0), (-0.12, -0.18, -SPREAD), ARM),  # left arm
    box_faces((-2, 0, -2), (4, 12, 4), (-1.9, 12, 0), (0.08, 0, 0), LEG),
    box_faces((-2, 0, -2), (4, 12, 4), (1.9, 12, 0), (0.08, 0, 0), LEG),
]

LIGHT = (-0.35, -0.8, -0.5)
ln = math.sqrt(sum(v * v for v in LIGHT))
LIGHT = tuple(v / ln for v in LIGHT)

GROUND_Y = H - round(4.45 * TEXEL)         # screen y of the feet
FEET = 24.0
def to_screen(p):
    return (W / 2 + p[0] * TEXEL, GROUND_Y + (p[1] - FEET) * TEXEL)

# --- Background: warm sunrise, big square sun with rays behind the character, grass blocks ---
from PIL import ImageFilter
img = Image.new("RGB", (W, H))
px = img.load()
SUN_C = (W / 2, GROUND_Y - 25.5 * TEXEL)     # behind the head
for y in range(H):
    t = y / H
    if t < 0.45:
        c = lerp((70, 92, 190), (196, 120, 190), t / 0.45)
    else:
        c = lerp((196, 120, 190), (255, 170, 90), (t - 0.45) / 0.45)
    for x in range(W):
        dx, dy = x - SUN_C[0], y - SUN_C[1]
        ang = math.atan2(dy, dx)
        dist = math.hypot(dx, dy) / max(W, H)
        ray = 0.5 + 0.5 * math.cos(ang * 12)
        glow = max(0.0, 1 - dist / 0.75)
        c2 = lerp(c, (255, 236, 170), 0.55 * glow * glow + 0.22 * ray * glow)
        px[x, y] = tuple(round(v) for v in c2)

def fill(x0, y0, x1, y1, color):
    for y in range(max(0, int(y0)), min(H, int(y1))):
        for x in range(max(0, int(x0)), min(W, int(x1))):
            px[x, y] = color

T = TEXEL
half = 8 * T
fill(SUN_C[0] - half, SUN_C[1] - half, SUN_C[0] + half, SUN_C[1] + half, (255, 214, 92))
fill(SUN_C[0] - half + 2 * T, SUN_C[1] - half + 2 * T, SUN_C[0] + half - 2 * T, SUN_C[1] + half - 2 * T, (255, 236, 140))
fill(SUN_C[0] - half + 4.5 * T, SUN_C[1] - half + 4.5 * T, SUN_C[0] + half - 4.5 * T, SUN_C[1] + half - 4.5 * T, (255, 250, 205))

grass = [(106, 178, 64), (94, 165, 52), (124, 192, 76)]
dirt = [(134, 96, 67), (121, 85, 58), (150, 108, 74), (104, 74, 50)]
cols = math.ceil(W / T) + 1
ground = {}
for gx in range(-cols, cols):
    edge = 2 + (1 if rng.random() < 0.4 else 0)
    for gy in range(0, 8):
        ground[(gx, gy)] = rng.choice(grass) if gy < edge else rng.choice(dirt)
for y in range(GROUND_Y, H):
    gy = int((y - GROUND_Y) / T)
    for x in range(W):
        gx = math.floor((x - W / 2) / T)
        px[x, y] = ground[(gx, gy)]
for y in range(GROUND_Y, GROUND_Y + int(1.5 * T)):
    for x in range(int(W / 2 - 6 * T), int(W / 2 + 6 * T)):
        px[x, y] = shade(px[x, y], 0.72)

background = img
img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
px = img.load()

# --- Rasterize faces back to front ---
faces = []
for face_list, rotation in parts:
    for p00, p10, p01, texture, normal in face_list:
        n = rot(normal, *rotation)
        if n[2] >= 0:          # facing away from the viewer at -z
            continue
        depth = (p10[2] + p01[2]) / 2
        light = 0.62 + 0.38 * max(0.0, n[0] * LIGHT[0] + n[1] * LIGHT[1] + n[2] * LIGHT[2])
        faces.append((depth, p00, p10, p01, texture, light))
faces.sort(key=lambda f: -f[0])

for depth, p00, p10, p01, texture, light in faces:
    a, b, c = to_screen(p00), to_screen(p10), to_screen(p01)
    ux, uy = b[0] - a[0], b[1] - a[1]
    vx, vy = c[0] - a[0], c[1] - a[1]
    det = ux * vy - uy * vx
    if abs(det) < 1e-6:
        continue
    xs = [a[0], b[0], c[0], b[0] + vx]
    ys = [a[1], b[1], c[1], b[1] + vy]
    rows, cols_t = len(texture), len(texture[0])
    for y in range(max(0, int(min(ys))), min(H, int(max(ys)) + 1)):
        for x in range(max(0, int(min(xs))), min(W, int(max(xs)) + 1)):
            dx, dy = x + 0.5 - a[0], y + 0.5 - a[1]
            u = (dx * vy - dy * vx) / det
            v = (ux * dy - uy * dx) / det
            if 0 <= u < 1 and 0 <= v < 1:
                px[x, y] = shade(texture[int(v * rows)][int(u * cols_t)], light) + (255,)

# Dark outline around the character so it reads at small sizes.
alpha = img.split()[3]
outline_mask = alpha.filter(ImageFilter.MaxFilter(2 * round(0.9 * SS) + 1))
outline = Image.new("RGBA", (W, H), (40, 24, 40, 255))
background = background.convert("RGBA")
background.paste(outline, (0, 0), outline_mask)
background.alpha_composite(img)
background.convert("RGB").resize((args.width, args.height), Image.LANCZOS).save(args.output)
