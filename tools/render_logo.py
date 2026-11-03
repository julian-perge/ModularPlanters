# /// script
# requires-python = ">=3.12"
# dependencies = ["pillow>=11"]
# ///
"""Draw the Modular Planters logo, a small isometric multiblock, from the mod's own block textures.

Run with `uv run tools/render_logo.py`. It writes docs/media/logo.png (512x512, for the project pages)
and src/main/resources/logo.png (256x256, the NeoForge mod list logo). Re-run it after changing block textures.
"""
import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageEnhance

REPO = Path(__file__).resolve().parent.parent
TEXTURES = REPO / "src/main/resources/assets/modular_planters/textures/block"
OUTPUTS = {REPO / "docs/media/logo.png": 512, REPO / "src/main/resources/logo.png": 256}
TEXTURE_SIZE = 256
BLOCK_WIDTH = 280
RENDER_SIZE = 1024
MARGIN = 0.04
MIN_CORE_SHARE = 0.01

FACES = {
    "core": ("core_top", "core_side"),
    "planter": ("planter_top", "planter_side_on"),
    "power": ("power_top", "power_side"),
    "fertilizer": ("fertilizer_top", "fertilizer_side"),
    "export": ("export_top", "export_side"),
    "frame": ("frame_side", "frame_side"),
}

# The keys are grid positions (x, y, z), where z is up and a larger x + y is nearer the viewer. The core sits
# in the middle, with the power, fertilizer and export blocks behind it and a row of planter blocks in front.
# The two raised planter blocks stand on the left and right corners, where they don't hide the core.
LAYOUT = {
    (0, 0, 0): "power", (1, 0, 0): "fertilizer", (2, 0, 0): "export",
    (0, 1, 0): "frame", (1, 1, 0): "core", (2, 1, 0): "frame",
    (0, 2, 0): "planter", (1, 2, 0): "planter", (2, 2, 0): "planter",
    (0, 2, 1): "planter", (2, 0, 1): "planter",
}


def texture(name):
    image = Image.open(TEXTURES / f"{name}.png").convert("RGBA")
    return image.resize((TEXTURE_SIZE, TEXTURE_SIZE), Image.NEAREST)


def affine_to_texture(corners):
    """Return the affine coefficients that map an output pixel back to a texture pixel.

    The three corners are where the texture's (0, 0), (size, 0) and (0, size) land in the output.
    """
    (x0, y0), (x1, y1), (x2, y2) = corners
    det = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
    s = TEXTURE_SIZE
    a = s * (y2 - y0) / det
    b = -s * (x2 - x0) / det
    d = -s * (y1 - y0) / det
    e = s * (x1 - x0) / det
    return (a, b, -a * x0 - b * y0, d, e, -d * x0 - e * y0)


def draw_face(canvas, image, corners, brightness):
    warped = image.transform(canvas.size, Image.AFFINE, affine_to_texture(corners), resample=Image.NEAREST)
    if brightness != 1:
        warped = ImageEnhance.Brightness(warped).enhance(brightness)
    p0, p1, p2 = corners
    p3 = (p1[0] + p2[0] - p0[0], p1[1] + p2[1] - p0[1])
    mask = Image.new("L", canvas.size, 0)
    ImageDraw.Draw(mask).polygon([p0, p1, p3, p2], fill=255)
    canvas.paste(warped, (0, 0), ImageChops.multiply(warped.getchannel("A"), mask))


def draw_cube(canvas, part, ox, oy, w):
    top, side = (texture(name) for name in FACES[part])
    h = 0.577 * w
    draw_face(canvas, top, [(ox + w / 2, oy), (ox + w, oy + w / 4), (ox, oy + w / 4)], 1.0)
    draw_face(canvas, side, [(ox, oy + w / 4), (ox + w / 2, oy + w / 2), (ox, oy + w / 4 + h)], 0.8)
    draw_face(canvas, side, [(ox + w / 2, oy + w / 2), (ox + w, oy + w / 4), (ox + w / 2, oy + w / 2 + h)], 0.62)


def render(layout):
    w = BLOCK_WIDTH
    canvas = Image.new("RGBA", (5 * w, 5 * w), (0, 0, 0, 0))
    left, top = canvas.width / 2 - w / 2, w
    for x, y, z in sorted(layout, key=lambda p: (p[0] + p[1], p[2])):
        draw_cube(canvas, layout[(x, y, z)], left + (x - y) * w / 2, top + (x + y) * w / 4 - z * 0.577 * w, w)
    canvas = canvas.crop(canvas.getbbox())
    side = round(max(canvas.size) * (1 + 2 * MARGIN))
    square = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    square.paste(canvas, ((side - canvas.width) // 2, (side - canvas.height) // 2))
    return square.resize((RENDER_SIZE, RENDER_SIZE), Image.LANCZOS)


def visible_core_share(logo):
    """Return the share of the logo's pixels that change when the core is drawn as a frame instead.

    A layout that hides the core behind other blocks returns 0, because swapping it changes nothing.
    """
    without_core = render({pos: "frame" if part == "core" else part for pos, part in LAYOUT.items()})
    changed = ImageChops.difference(logo, without_core).convert("L").point(lambda v: 255 if v else 0)
    return changed.histogram()[255] / (RENDER_SIZE * RENDER_SIZE)


def main():
    logo = render(LAYOUT)
    share = visible_core_share(logo)
    if share < MIN_CORE_SHARE:
        sys.exit(f"the core covers {share:.1%} of the logo, under {MIN_CORE_SHARE:.0%}; move the blocks in front of it")
    for path, size in OUTPUTS.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        logo.resize((size, size), Image.LANCZOS).save(path)
        print(f"wrote {path.relative_to(REPO)} ({size}x{size})")


if __name__ == "__main__":
    main()
