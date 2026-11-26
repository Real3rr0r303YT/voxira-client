#!/usr/bin/env python3
"""Generates the artwork for the Voxira title screen.

Everything is drawn procedurally (no external images), so the art is ours and
can be tweaked by changing the numbers below. Run from the repo root:

    python3 tools/generate_menu_assets.py

Requires Pillow and numpy. Output: src/main/resources/assets/voxira/textures/gui/menu/
"""
import json
import math
import os
import random

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

OUT = os.path.join("src", "main", "resources", "assets", "voxira", "textures", "gui", "menu")
W, H = 1920, 1080
HORIZON = 0.66  # horizon height as fraction of the canvas

# Palette
NIGHT = (6, 5, 22)
DEEP = (24, 12, 58)
VIOLET = (92, 48, 168)
MAGENTA = (196, 92, 220)
ACCENT = (139, 108, 255)
LILAC = (214, 196, 255)

rng = random.Random(1337)
nrng = np.random.default_rng(1337)


def save(img, name, blur=True):
    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, name)
    img.save(path, optimize=True)
    with open(path + ".mcmeta", "w") as f:
        json.dump({"texture": {"blur": blur, "clamp": True}}, f, indent=2)
    print(f"{path}  {img.size[0]}x{img.size[1]}  {os.path.getsize(path) // 1024} KB")


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def gradient_column(stops, height):
    """stops: list of (position 0..1, rgb)."""
    col = np.zeros((height, 3), dtype=np.float32)
    for y in range(height):
        p = y / (height - 1)
        for (p0, c0), (p1, c1) in zip(stops, stops[1:]):
            if p0 <= p <= p1:
                t = (p - p0) / max(1e-6, p1 - p0)
                t = t * t * (3 - 2 * t)
                col[y] = [c0[i] + (c1[i] - c0[i]) * t for i in range(3)]
                break
    return col


def soft_noise(w, h, scale, seed):
    g = np.random.default_rng(seed).random((h // scale + 2, w // scale + 2)).astype(np.float32)
    img = Image.fromarray((g * 255).astype(np.uint8)).resize((w, h), Image.BICUBIC)
    return np.asarray(img.filter(ImageFilter.GaussianBlur(scale)), dtype=np.float32) / 255


# ---------------------------------------------------------------- sky
def make_sky():
    stops = [(0.0, NIGHT), (0.35, DEEP), (0.56, VIOLET), (HORIZON, MAGENTA), (0.78, (120, 50, 150)), (1.0, (30, 14, 50))]
    col = gradient_column(stops, H)
    sky = np.repeat(col[:, None, :], W, axis=1)

    # Nebula clouds
    n = soft_noise(W, H, 160, 7) * 0.6 + soft_noise(W, H, 60, 8) * 0.4
    yy = np.linspace(0, 1, H)[:, None]
    band = np.exp(-((yy - 0.38) ** 2) / 0.03)
    neb = np.clip((n - 0.45) * 2.2, 0, 1) * band
    sky += neb[..., None] * np.array([70, 40, 120], dtype=np.float32)

    # Horizon glow under the eclipse
    xx = np.linspace(0, 1, W)[None, :]
    glow = np.exp(-(((xx - 0.72) / 0.3) ** 2) - ((yy - HORIZON) / 0.1) ** 2)
    sky += glow[..., None] * np.array([90, 50, 110], dtype=np.float32)

    img = Image.fromarray(np.clip(sky, 0, 255).astype(np.uint8), "RGB").convert("RGBA")

    # Stars
    stars = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(stars)
    for _ in range(900):
        x = rng.random() * W
        y = (rng.random() ** 1.6) * H * 0.62
        fade = 1 - y / (H * 0.62)
        b = rng.random() ** 3
        a = int(255 * fade * (0.25 + 0.75 * b))
        r = 0.6 + b * 1.4
        tint = lerp((255, 255, 255), LILAC, rng.random())
        d.ellipse([x - r, y - r, x + r, y + r], fill=tint + (a,))
    halo = stars.filter(ImageFilter.GaussianBlur(3))
    img = Image.alpha_composite(img, halo)
    img = Image.alpha_composite(img, stars)
    save(img.convert("RGB"), "sky.png")


# ---------------------------------------------------------------- eclipse
def make_eclipse():
    S = 1024
    c = S / 2
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    r = np.hypot(xx - c, yy - c)
    R = 230.0

    corona = np.exp(-np.clip(r - R, 0, None) / 70) * (r >= R - 2)
    corona2 = np.exp(-np.clip(r - R, 0, None) / 180) * (r >= R - 2) * 0.55
    ring = np.exp(-((r - R) / 5.0) ** 2)
    inner = np.exp(-((r - (R - 6)) / 14.0) ** 2) * 0.35

    # Brighter on the top-right, like light leaking around the edge
    ang = np.arctan2(yy - c, xx - c)
    bias = 0.65 + 0.35 * np.cos(ang + math.pi / 4)

    rgb = np.zeros((S, S, 3), dtype=np.float32)
    a = np.zeros((S, S), dtype=np.float32)

    def add(mask, color, alpha):
        nonlocal rgb, a
        m = np.clip(mask * alpha, 0, 1)
        rgb = rgb * (1 - m[..., None]) + np.array(color, dtype=np.float32) * m[..., None]
        a = a + m * (1 - a)

    add(corona2 * bias, MAGENTA, 0.8)
    add(corona * bias, ACCENT, 0.95)
    add(inner, VIOLET, 1.0)
    disc = np.clip(R - r, 0, 1)
    add(disc, (10, 7, 24), 1.0)
    add(ring * (0.7 + 0.3 * bias), (248, 236, 255), 1.0)

    # Diamond-ring flare
    fx, fy = c + R * math.cos(-math.pi / 4), c + R * math.sin(-math.pi / 4)
    fr = np.hypot(xx - fx, yy - fy)
    add(np.exp(-(fr / 26) ** 2), (255, 250, 255), 1.0)
    add(np.exp(-(fr / 90) ** 2) * 0.6, LILAC, 1.0)

    # Fade everything to zero before the texture edge so no hard border shows
    edge = np.clip((c - 8 - r) / 120.0, 0, 1)
    a = a * (edge * edge * (3 - 2 * edge))
    img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.clip(a * 255, 0, 255)]).astype(np.uint8), "RGBA")
    save(img, "eclipse.png")


# ---------------------------------------------------------------- crystal mountains (far)
def make_mountains():
    LH = 620
    base = Image.new("RGBA", (W, LH), (0, 0, 0, 0))
    d = ImageDraw.Draw(base)
    light_x = W * 0.5

    peaks = []
    x = -120
    while x < W + 120:
        w = rng.uniform(160, 340)
        h = rng.uniform(170, 430)
        # Keep the center lower so the eclipse stays visible
        center = abs((x + w / 2) - W / 2) / (W / 2)
        h *= 0.3 + 0.8 * center ** 1.3
        peaks.append((x, w, h))
        x += w * rng.uniform(0.45, 0.7)

    for px, pw, ph in sorted(peaks, key=lambda p: -p[2]):
        top = (px + pw * rng.uniform(0.38, 0.62), LH - ph)
        left = (px, LH)
        right = (px + pw, LH)
        mid = (top[0] + rng.uniform(-pw * 0.08, pw * 0.08), LH - ph * rng.uniform(0.25, 0.45))
        lit_left = top[0] < light_x
        dark = (34, 18, 70, 255)
        lit = (62, 36, 118, 255)
        d.polygon([top, left, mid], fill=lit if not lit_left else dark)
        d.polygon([top, mid, right], fill=dark if not lit_left else lit)
        d.line([left, top, right], fill=(180, 140, 255, 170), width=2)
        d.line([top, mid], fill=(120, 90, 210, 90), width=1)

    # Fade the base into the horizon haze
    arr = np.asarray(base, dtype=np.float32)
    yy = np.linspace(0, 1, LH)[:, None]
    haze = np.clip((yy - 0.55) / 0.45, 0, 1) * 0.55
    arr[..., :3] = arr[..., :3] * (1 - haze[..., None]) + np.array([96, 44, 130]) * haze[..., None]
    img = Image.fromarray(arr.astype(np.uint8), "RGBA")
    glow = img.filter(ImageFilter.GaussianBlur(8))
    ga = np.asarray(glow, dtype=np.float32)
    ga[..., 3] *= 0.35
    out = Image.alpha_composite(Image.fromarray(ga.astype(np.uint8), "RGBA"), img)
    save(out, "mountains.png")


# ---------------------------------------------------------------- voxel hills (near)
def make_hills():
    LH = 440
    BLOCK = 24
    img = Image.new("RGBA", (W, LH), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cols = W // BLOCK + 2
    heights = []
    for i in range(cols):
        t = i / cols
        valley = 1 - 0.55 * math.exp(-((t - 0.5) / 0.22) ** 2)
        hgt = (math.sin(t * 7.3) * 0.5 + math.sin(t * 17.1 + 1.3) * 0.25 + 1.2) * valley
        heights.append(max(2, int(hgt * 6)))

    body = (12, 9, 26, 255)
    side = (20, 14, 40, 255)
    for i, hb in enumerate(heights):
        x0 = i * BLOCK
        top = LH - hb * BLOCK
        d.rectangle([x0, top, x0 + BLOCK - 1, LH], fill=body)
        # Block grid, subtle
        for j in range(hb):
            y = top + j * BLOCK
            d.rectangle([x0, y, x0 + BLOCK - 1, y + BLOCK - 1], outline=side)
        # Lit top edge
        d.rectangle([x0, top, x0 + BLOCK - 1, top + 2], fill=(150, 112, 255, 255))
        # Step edges where the neighbor is lower
        for nb, edge_x in ((i - 1, x0), (i + 1, x0 + BLOCK - 2)):
            if 0 <= nb < len(heights) and heights[nb] < hb:
                d.rectangle([edge_x, top, edge_x + 1, LH - heights[nb] * BLOCK], fill=(90, 66, 180, 255))

    # Glowing crystal shards on some columns
    shards = Image.new("RGBA", (W, LH), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shards)
    for i, hb in enumerate(heights):
        if rng.random() < 0.18:
            cx = i * BLOCK + BLOCK / 2
            base_y = LH - hb * BLOCK
            sh = rng.uniform(18, 40)
            sw = rng.uniform(5, 9)
            sd.polygon([(cx, base_y - sh), (cx - sw, base_y), (cx + sw, base_y)], fill=(196, 160, 255, 255))
            sd.polygon([(cx, base_y - sh), (cx, base_y), (cx + sw, base_y)], fill=(139, 108, 255, 255))
    glow = shards.filter(ImageFilter.GaussianBlur(10))
    img = Image.alpha_composite(img, glow)
    img = Image.alpha_composite(img, glow)
    img = Image.alpha_composite(img, shards)
    save(img, "hills.png")


# ---------------------------------------------------------------- emblem
def make_emblem():
    S = 512
    SS = 3  # supersampling
    big = Image.new("RGBA", (S * SS, S * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    c = S * SS / 2
    k = SS

    def P(x, y):
        return (c + x * k, c + y * k)

    # Outer hexagon frame
    hexa = [P(170 * math.cos(math.radians(a)), 170 * math.sin(math.radians(a))) for a in range(-90, 270, 60)]
    d.polygon(hexa, outline=(214, 196, 255, 255), width=6 * k)
    inner = [P(150 * math.cos(math.radians(a)), 150 * math.sin(math.radians(a))) for a in range(-90, 270, 60)]
    d.polygon(inner, outline=(139, 108, 255, 200), width=2 * k)

    # Crystal V: two faceted arms meeting at the bottom tip
    tip = P(0, 112)
    lt_out, lt_in = P(-118, -92), P(-62, -92)
    rt_in, rt_out = P(62, -92), P(118, -92)
    notch = P(0, 18)
    # Left arm, two facets
    d.polygon([lt_out, lt_in, notch, tip], fill=(240, 232, 255, 255))
    d.polygon([lt_out, P(-90, -92), tip], fill=(196, 172, 255, 255))
    # Right arm, two facets (darker side)
    d.polygon([rt_in, rt_out, tip, notch], fill=(139, 108, 255, 255))
    d.polygon([P(90, -92), rt_out, tip], fill=(98, 70, 210, 255))
    # Center gem
    d.polygon([P(0, -70), P(-22, -40), P(0, -8), P(22, -40)], fill=(255, 255, 255, 255))
    d.polygon([P(0, -70), P(0, -8), P(22, -40)], fill=(196, 172, 255, 255))
    # Small ticks on the frame
    for a in range(-90, 270, 60):
        x, y = 170 * math.cos(math.radians(a)), 170 * math.sin(math.radians(a))
        d.ellipse([P(x - 7, y - 7), P(x + 7, y + 7)], fill=(255, 255, 255, 255))

    sharp = big.resize((S, S), Image.LANCZOS)
    glow = sharp.filter(ImageFilter.GaussianBlur(18))
    ga = np.asarray(glow, dtype=np.float32)
    ga[..., :3] = np.array(ACCENT, dtype=np.float32)
    ga[..., 3] = np.clip(ga[..., 3] * 1.6, 0, 255)
    out = Image.alpha_composite(Image.fromarray(ga.astype(np.uint8), "RGBA"), sharp)
    save(out, "emblem.png")
    save(Image.fromarray(ga.astype(np.uint8), "RGBA"), "emblem_glow.png")


# ---------------------------------------------------------------- pill + icons
def make_pill():
    # 3 slices: caps 32px each + 64px middle, height 64. Drawn white, tinted in game.
    PW, PH, SS = 128, 64, 4
    big = Image.new("RGBA", (PW * SS, PH * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    d.rounded_rectangle([0, 0, PW * SS - 1, PH * SS - 1], radius=PH * SS // 2, fill=(255, 255, 255, 255))
    save(big.resize((PW, PH), Image.LANCZOS), "pill.png")

    big = Image.new("RGBA", (PW * SS, PH * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    w = 2 * SS
    d.rounded_rectangle([0, 0, PW * SS - 1, PH * SS - 1], radius=PH * SS // 2, outline=(255, 255, 255, 255), width=w)
    save(big.resize((PW, PH), Image.LANCZOS), "pill_outline.png")

    # Soft round glow used behind buttons and icons
    G = 128
    yy, xx = np.mgrid[0:G, 0:G].astype(np.float32)
    r = np.hypot(xx - G / 2, yy - G / 2) / (G / 2)
    a = np.clip(1 - r, 0, 1) ** 2
    glow = np.dstack([np.full_like(a, 255)] * 3 + [a * 255]).astype(np.uint8)
    save(Image.fromarray(glow, "RGBA"), "glow.png")


def make_icons():
    S, SS = 64, 4
    k = SS

    def canvas():
        im = Image.new("RGBA", (S * SS, S * SS), (0, 0, 0, 0))
        return im, ImageDraw.Draw(im)

    def done(im, name):
        save(im.resize((S, S), Image.LANCZOS), name)

    white = (255, 255, 255, 255)

    # Options: gear
    im, d = canvas()
    c = S * SS / 2
    for i in range(8):
        a = math.radians(i * 45)
        x, y = c + math.cos(a) * 20 * k, c + math.sin(a) * 20 * k
        d.ellipse([x - 6 * k, y - 6 * k, x + 6 * k, y + 6 * k], fill=white)
    d.ellipse([c - 19 * k, c - 19 * k, c + 19 * k, c + 19 * k], fill=white)
    d.ellipse([c - 8 * k, c - 8 * k, c + 8 * k, c + 8 * k], fill=(0, 0, 0, 0))
    done(im, "icon_options.png")

    # Mods: 2x2 blocks
    im, d = canvas()
    for gx in (0, 1):
        for gy in (0, 1):
            x0 = (12 + gx * 22) * k
            y0 = (12 + gy * 22) * k
            d.rounded_rectangle([x0, y0, x0 + 18 * k, y0 + 18 * k], radius=4 * k, fill=white if (gx + gy) % 2 == 0 else (255, 255, 255, 150))
    done(im, "icon_mods.png")

    # Website: globe
    im, d = canvas()
    w = 4 * k
    d.ellipse([10 * k, 10 * k, 54 * k, 54 * k], outline=white, width=w)
    d.ellipse([22 * k, 10 * k, 42 * k, 54 * k], outline=white, width=w)
    d.line([12 * k, 32 * k, 52 * k, 32 * k], fill=white, width=w)
    d.line([32 * k, 11 * k, 32 * k, 53 * k], fill=white, width=w)
    done(im, "icon_web.png")

    # Quit: power
    im, d = canvas()
    d.arc([12 * k, 14 * k, 52 * k, 54 * k], start=-60, end=240, fill=white, width=5 * k)
    d.rounded_rectangle([29.5 * k, 8 * k, 34.5 * k, 32 * k], radius=2.5 * k, fill=white)
    done(im, "icon_quit.png")


if __name__ == "__main__":
    make_sky()
    make_eclipse()
    make_mountains()
    make_hills()
    make_emblem()
    make_pill()
    make_icons()
