---
name: EduNexus Meta-Optimistic
colors:
  surface: '#faf8ff'
  surface-dim: '#d8d9e4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3fe'
  surface-container: '#ecedf8'
  surface-container-high: '#e6e7f2'
  surface-container-highest: '#e1e2ec'
  on-surface: '#191b23'
  on-surface-variant: '#424754'
  inverse-surface: '#2e3038'
  inverse-on-surface: '#eff0fb'
  outline: '#727786'
  outline-variant: '#c2c6d6'
  surface-tint: '#0059c8'
  primary: '#004db0'
  on-primary: '#ffffff'
  primary-container: '#0064e0'
  on-primary-container: '#e6ebff'
  inverse-primary: '#afc6ff'
  secondary: '#5e5e5e'
  on-secondary: '#ffffff'
  secondary-container: '#e2e2e2'
  on-secondary-container: '#646464'
  tertiary: '#913400'
  on-tertiary: '#ffffff'
  tertiary-container: '#b94500'
  on-tertiary-container: '#ffe7df'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d9e2ff'
  primary-fixed-dim: '#afc6ff'
  on-primary-fixed: '#001944'
  on-primary-fixed-variant: '#004299'
  secondary-fixed: '#e2e2e2'
  secondary-fixed-dim: '#c6c6c6'
  on-secondary-fixed: '#1b1b1b'
  on-secondary-fixed-variant: '#474747'
  tertiary-fixed: '#ffdbcd'
  tertiary-fixed-dim: '#ffb597'
  on-tertiary-fixed: '#360f00'
  on-tertiary-fixed-variant: '#7e2c00'
  background: '#faf8ff'
  on-background: '#191b23'
  surface-variant: '#e1e2ec'
  success: '#31A24C'
  attention: '#F2A918'
  critical: '#E41E3F'
  canvas: '#FFFFFF'
  surface-soft: '#F1F4F7'
  ink-deep: '#0A1317'
  ink: '#1C1E21'
  steel: '#5D6C7B'
  disabled-text: '#BCC0C4'
  hairline: '#CED0D4'
  hairline-soft: '#DEE3E9'
typography:
  display-lg:
    fontFamily: Montserrat
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Montserrat
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Montserrat
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-md:
    fontFamily: Montserrat
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: plusJakartaSans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: plusJakartaSans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: plusJakartaSans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: plusJakartaSans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.01em
  code-sm:
    fontFamily: jetbrainsMono
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 20px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 16px
  lg: 20px
  xxl: 32px
  section: 64px
  section-lg: 80px
---

# EduNexus — UI Design Prompts (Meta Design System)

> **Source**: SRS v2 (02_EduNexus_SRS_v2.pdf) + Screen Flow (29_06_2026 Excel)
> **Design System**: Meta Design System (`DESIGN.md`) — Optimistic VF typography, cobalt primary `{colors.primary}` (#0064E0), ink-button black CTAs, pill-shaped `{rounded.full}` buttons, `{rounded.xxxl}` cards on `{colors.canvas}` white surfaces.

---

## 🎨 Design System Mapping: Meta → EduNexus

The Meta design system is adapted for an EdTech SaaS context. Below maps Meta's hardware-commerce tokens to EduNexus's learning-platform needs:

```yaml
Brand Adaptation:
  # Typography — Optimistic VF as universal face
  font-family: "Optimistic VF", Montserrat, Helvetica, Arial, Noto Sans
  font-features: "ss01, ss02" on all headings
  code-font: "JetBrains Mono" (Markdown editor only — addition to Meta system)

  # Color Mapping
  Primary Action (CTA):       "{colors.primary}" (#0064E0)  — "Xuất bản", "Nộp bài", "Bắt đầu học"
  Marketing/Nav Action:       "{colors.ink-button}" (#000000) — "Đăng nhập", sidebar nav
  Success:                    "{colors.success}" (#31A24C) — completed, approved, correct
  Warning/Attention:          "{colors.attention}" (#F2A918) — draft, pending, near-deadline
  Critical/Error:             "{colors.critical}" (#E41E3F) — expired, rejected, error, wrong
  Canvas (page bg):           "{colors.canvas}" (#FFFFFF)
  Surface (card bg, inputs):  "{colors.surface-soft}" (#F1F4F7)
  Deep Ink (headings):        "{colors.ink-deep}" (#0A1317)
  Ink (body text):            "{colors.ink}" (#1C1E21)
  Secondary text:             "{colors.steel}" (#5D6C7B)
  Disabled:                   "{colors.disabled-text}" (#BCC0C4)
  Borders:                    "{colors.hairline}" (#CED0D4) inputs
                              "{colors.hairline-soft}" (#DEE3E9) cards, dividers

  # Shape Mapping
  Buttons/Badges/Pills:       "{rounded.full}" (100px) — ALWAYS pill-shaped
  Feature Cards:              "{rounded.xxxl}" (32px) — course cards, hero cards
  Standard Cards:             "{rounded.xl}" (16px) — info tiles, accordion items
  Inputs:                     "{rounded.lg}" (8px)
  Avatars/Swatches:           "{rounded.circle}" (9999px)

  # Elevation (mostly flat)
  Level 0: No shadow, hairline-soft border — default cards
  Level 2: rgba(20,22,26,0.3) 0px 1px 4px — sticky sidebar, purchase panels

  # Spacing
  Card padding:               "{spacing.xxl}" (32px)
  Section gaps:               "{spacing.section}" (64px) to "{spacing.section-lg}" (80px)
  Tight internal:             "{spacing.base}" (16px) to "{spacing.lg}" (20px)
```