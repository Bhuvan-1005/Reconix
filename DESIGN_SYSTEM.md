# Reconix Design System
## Fintech UI — Color Palette, Typography & Logo Brief

---

## Color Palette

| Role | Token Name | Hex | Usage |
|------|-----------|-----|-------|
| Screen Background | `DeepSlateBlue` | `#060E20` | Root `Box` background on all screens |
| Card Surface | `SlateBlue800` / `NavyCard` | `#0D1B3E` | All card container backgrounds |
| Surface Variant | `SlateBlue700` | `#122250` | Card gradient highlight (top) |
| Card Border | `SlateBlue600` | `#1A3066` | Dividers, card borders, hairline strokes |
| ★ Match / Success | `EmeraldMatch` | `#00C896` | MATCHED status, approve buttons, profit figures |
| Match Dark | `EmeraldMatchDark` | `#00A07A` | Gradient end for emerald elements |
| ★ Mismatch / Error | `CrimsonMismatch` | `#FF2D5B` | MISMATCH status, reject buttons, loss figures |
| Mismatch Dark | `CrimsonMismatchDark` | `#CC1A43` | Gradient end for crimson elements |
| Pending / Review | `GoldPending` | `#FFB800` | PENDING & MANUAL_REVIEW states |
| Primary Action | `ElectricIndigo` | `#4F7FFF` | Buttons, links, active tab indicator |
| Data Accent | `NeonCyan` | `#00D9FF` | Secondary data highlights, sub-labels |
| Primary Labels | `PureWhite` | `#FFFFFF` | All primary headings and body text |
| Secondary Labels | `SilverText` | `#B8C8E8` | Subtitles, vendor names, dates |
| Muted Labels | `MutedText` | `#6B82B0` | CAPS section labels, disabled text |

---

## Design Grid — 8dp Base Unit

| Spacing | dp | Use |
|---------|----|-----|
| XS | 4dp | Icon inner padding, dot spacing |
| S | 8dp | Row gaps, badge padding horizontal |
| M | 16dp | Card inner padding, section gaps |
| L | 24dp | Screen section spacers |
| XL | 32dp | Hero section top padding |
| XXL | 48dp | Full-screen vertical centering margin |

---

## Typography

| Style | Size | Weight | Font | Color |
|-------|------|--------|------|-------|
| Screen Title | 28 sp | Bold | Monospace | `PureWhite` |
| Card Heading | 18 sp | Bold | Default | `PureWhite` |
| KPI Number | 28 sp | Bold | Monospace | `PureWhite` |
| Amount | 22 sp | Bold | Monospace | `ElectricIndigo` |
| Section Label | 9–10 sp | Bold | Default | `MutedText` + 1.0–1.2sp letterSpacing |
| Body Text | 13–14 sp | Medium | Default | `SilverText` |
| Badge Text | 11 sp | Bold | Default | Status color + 0.5sp letterSpacing |

---

## Corner Radii

| Element | Radius |
|---------|--------|
| Full-screen cards | 20–24dp |
| Compact cards / list items | 16dp |
| Buttons | 12–14dp |
| Status badges / pills | 100dp (fully rounded) |
| Icon circles | 100dp (CircleShape) |
| Avatar initials | 100dp (CircleShape) |
| Tooltip / small chips | 6–8dp |

---

## Logo Concept Description

**App Name:** Reconix  
**Tagline:** Intelligent Invoice Validation

### Visual Description
A futuristic, minimalist icon combining a financial document with neural circuitry.

**Base Form:** A stylized document silhouette with a folded corner at the top-right — the classic "invoice" metaphor rendered in 3D isometric perspective. The document appears to float above its shadow on a deep navy background with a faint Royal Blue ambient glow.

**Circuit Transformation:** From the folded corner, thin Electric Royal Blue (`#4F7FFF`) circuit traces extend outward — branching at 90° nodes — suggesting machine intelligence running across the invoice data. The traces fade to near-invisible at the edges, giving a sense of infinite computation.

**The Checkmark:** A bold, luminous Neon Emerald (`#00C896`) checkmark cuts diagonally across the bottom-left of the document. Unlike a flat check, this one has a **neon trail effect** — the stroke is semi-transparent at the start, peaks at brilliant emerald white at the apex, and tapers to a fine point. It represents final validation — the definitive "MATCHED."

**Glow Aura:** The entire icon sits inside a soft radial glow — a blend of `#001A3E` (deep navy) to `#0D2B6E` (royal blue tint) — giving a floating, premium fintech feel with no hard background edge.

**Color Breakdown:**
- Document body: `#0D1B3E` (Royal Navy), edges highlighted with `#4F7FFF` 15% opacity
- Circuit traces: `#4F7FFF` (Electric Royal Blue), 60–80% opacity
- Circuit nodes: bright `#4F7FFF` dots, 100% opacity
- Checkmark stroke: `#00C896` → `#B0FFF0` (neon emerald to near-white at peak) → `#00C896`
- Background: transparent / `#060E20` depth gradient
- Shadow: `#001030` blur-20dp beneath document

**Style:** Clean, vector-flat with subtle depth cues. No text. No 3D extrusion — depth is implied by layering and shadow only. Suitable for App Store icon (1024×1024), splash screen (240×240), and `NavigationBar` icon derivative (24×24).

---

## Component Quick Reference

| Component | File | Key Design |
|-----------|------|-----------|
| `StatusBadge` | `ui/components/StatusBadge.kt` | Dot + ALL-CAPS pill, emerald/crimson/gold |
| `ActionStatusBadge` | same | Finance approval states |
| `MatchIndicator` | same | Dot + label row pill for comparison grids |
| `PriorityBadge` | same | RED/GOLD/BLUE urgency tag |
| `GlassCard` | `ui/components/GlassCard.kt` | Navy gradient + luminous border brush |
| `EmeraldGlassCard` | same | GlassCard with emerald top edge |
| `CrimsonGlassCard` | same | GlassCard with crimson top edge |
| `StatCard` | `ui/components/StatCard.kt` | Left accent bar + count-up monospace KPI |
| `InvoiceCard` | `ui/components/InvoiceCard.kt` | Status-colored top line + gradient surface |
| `CompactInvoiceCard` | same | Left accent bar slim row card |
| `EmptyStateView` | `ui/components/EmptyStateView.kt` | Double-ring icon glow, emerald tint |
| `SkeletonLoader` | `ui/components/SkeletonLoader.kt` | Navy diagonal shimmer sweep |
| `PulsingFab` | `ui/screens/FinanceDashboard.kt` | Infinite scale + ring animation |
