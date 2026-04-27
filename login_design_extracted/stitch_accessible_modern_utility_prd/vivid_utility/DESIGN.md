# Design System Specification: High-End Editorial Utility

## 1. Overview & Creative North Star
**The Creative North Star: "The Tactile Architect"**

This design system rejects the "templated" look of modern SaaS. It bridges the gap between high-end editorial print and functional utility. By combining the raw energy of **Clash Display** with a sophisticated, layered surface logic, we create an environment that feels curated rather than generated.

We move beyond the grid by utilizing **intentional asymmetry** and **tonal depth**. The "utility" aspect is expressed through absolute clarity in typography and high-contrast accents, while the "modern" feel is achieved through glassmorphism and the total elimination of structural lines. We don't use borders to define space; we use light and material.

---

## 2. Colors & Materiality
The palette is anchored by a "Papyrus & Ink" foundation, punctuated by a "Vivid Kinetic" orange.

### Color Tokens (Material Design Mapping)
- **Primary (Vivid Kinetic):** `#A63B00` (Core brand)
- **Primary Container:** `#FF5E00` (High-energy accents / Main CTAs)
- **Surface (The Base):** `#F9F9F7` (Off-white / Paper)
- **On-Surface:** `#1A1C1B` (Charcoal / High-contrast text)
- **Surface Container (Lowest):** `#FFFFFF` (Elevated "Paper" sheets)
- **Surface Container (Low):** `#F4F4F2` (Soft indentations)
- **Surface Container (High):** `#E8E8E6` (Section grouping)

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning. Boundaries must be defined solely through background color shifts or subtle tonal transitions. To separate a section, shift the background from `surface` to `surface-container-low`.

### The "Glass & Gradient" Rule
To prevent the UI from feeling "flat," use `backdrop-blur-md` on floating elements (e.g., the bottom nav). CTAs should utilize a subtle linear gradient from `primary` (#A63B00) to `primary-container` (#FF5E00) at a 135° angle to add visual "soul" and depth.

---

## 3. Typography: The Editorial Edge
We pair a brutalist heading font with a highly legible geometric sans to create an authoritative hierarchy.

- **Display & Headlines (Clash Display):** Use for all headers. This font carries a "signature" weight. Use `display-lg` for hero moments with tight letter-spacing (-2%).
- **Body & Titles (DM Sans / Manrope):** Use for all functional reading. `body-lg` (1rem) is the standard for long-form text to maintain premium readability.
- **Labeling:** Small-caps or heavy-weight labels (`label-md`) should be used for category headers to create an "archive" or "utility" feel.

---

## 4. Elevation & Depth: Tonal Layering
Depth is a physical property in this system, not a visual effect.

- **The Layering Principle:** Treat the UI as stacked sheets of fine paper. Place a `surface-container-lowest` (#FFFFFF) card on a `surface-container-low` (#F4F4F2) background to create a "lift" without a shadow.
- **Ambient Shadows:** When a floating state is required (e.g., a modal), use a "Whisper Shadow": `0px 24px 48px rgba(26, 28, 27, 0.06)`. This mimics natural ambient light.
- **Ghost Borders:** If accessibility requires a stroke (e.g., in high-contrast modes), use `outline-variant` at **15% opacity**. Never use a 100% opaque border.
- **Glassmorphism:** For elements like the Bottom Navigation or Header, use a semi-transparent `surface` color (alpha 80%) with `backdrop-blur-md`.

---

## 5. Components

### Navigation: The Floating Anchor
The bottom navigation bar is the system's "anchor."
- **Style:** A floating capsule (not edge-to-edge) using `surface-container-lowest` with 85% opacity and `backdrop-blur-md`.
- **Active State:** Use a `primary-container` (#FF5E00) icon with a 4px "kinetic dot" underneath.
- **Labels:** `label-sm` (DM Sans), shown only on the active item to reduce visual clutter.

### Settings & List Items: The Clean List
- **Grouping:** Group related settings within a `surface-container-low` rounded container (Radius: 20px).
- **List Items:** No dividers. Use 24px of vertical padding to define the hit area. 
- **Toggle Switches:** The track should be `surface-container-highest`. The thumb must be `surface-container-lowest`. When "On," the track shifts to `primary-container` (#FF5E00).
- **Leading Elements:** Icons should be encased in a subtle 40px circle of `surface-container-high` to provide a "Utility" look.

### Buttons: Kinetic Actions
- **Primary:** Gradient fill (Primary to Primary-Container), Radius: 20px (or `full`), White text.
- **Secondary:** `surface-container-high` background with `on-surface` text. No border.
- **Tertiary:** Pure text with `primary` color, bold weight, and a `primary-container` underline (2px) on hover.

---

## 6. Do’s and Don’ts

### Do
- **Do** use aggressive white space (24px, 32px, 48px) to separate functional groups.
- **Do** overlap elements slightly (e.g., a card bleeding over a section header) to create an editorial layout.
- **Do** use the `primary-container` orange sparingly—it is a laser-focused tool for attention, not a filler color.
- **Do** ensure all text on `primary-container` is `#FFFFFF` or `#370E00` (on_primary_fixed) for accessibility.

### Don’t
- **Don’t** use dividers or lines. If the content feels messy, increase the background contrast or the padding.
- **Don’t** use standard "Material Design" blue or grey for toggles and checkboxes. Stick to the Charcoal and Orange palette.
- **Don’t** use sharp corners. Everything follows the 20px (1.25rem) or `full` (pill) radius rule to maintain a "tactile" softness.
- **Don’t** use drop shadows on every card. Only the top-most floating layer (Nav/Modals) deserves a shadow.