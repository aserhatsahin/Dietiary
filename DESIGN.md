---
name: Dietiary
description: The dietitian's numbers, read like a Nutrition Facts label.
colors:
  ink: "#0b0b0b"
  ink-hover: "#2a2a2a"
  paper: "#ffffff"
  ground: "#ededed"
  mute: "#555555"
  faint: "#8a8a8a"
  toward: "#1a7f37"
  against: "#b42318"
typography:
  display:
    fontFamily: "Libre Franklin, Helvetica Neue, Arial, sans-serif"
    fontSize: "38px"
    fontWeight: 900
    lineHeight: 1.05
    letterSpacing: "-0.02em"
  headline:
    fontFamily: "Libre Franklin, Helvetica Neue, Arial, sans-serif"
    fontSize: "30px"
    fontWeight: 900
    lineHeight: 1.1
    letterSpacing: "-0.01em"
  title:
    fontFamily: "Libre Franklin, Helvetica Neue, Arial, sans-serif"
    fontSize: "22px"
    fontWeight: 900
    lineHeight: 1.15
  figure-lead:
    fontFamily: "Libre Franklin, Helvetica Neue, Arial, sans-serif"
    fontSize: "20px"
    fontWeight: 900
    lineHeight: 1.45
    fontFeature: "tnum"
  body:
    fontFamily: "Libre Franklin, Helvetica Neue, Arial, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.45
    fontFeature: "tnum"
  body-strong:
    fontFamily: "Libre Franklin, Helvetica Neue, Arial, sans-serif"
    fontSize: "14px"
    fontWeight: 700
    lineHeight: 1.45
    fontFeature: "tnum"
  aside:
    fontFamily: "Libre Franklin, Helvetica Neue, Arial, sans-serif"
    fontSize: "13px"
    fontWeight: 400
    lineHeight: 1.45
  label:
    fontFamily: "Libre Franklin, Helvetica Neue, Arial, sans-serif"
    fontSize: "12px"
    fontWeight: 700
    lineHeight: 1.45
    fontFeature: "tnum"
  footnote:
    fontFamily: "Libre Franklin, Helvetica Neue, Arial, sans-serif"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: 1.45
rounded:
  none: "0px"
spacing:
  "4": "4px"
  "8": "8px"
  "12": "12px"
  "16": "16px"
  "20": "20px"
  "24": "24px"
  "32": "32px"
  "48": "48px"
components:
  button-primary:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.paper}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.none}"
    padding: "8px 16px"
  button-primary-hover:
    backgroundColor: "{colors.ink-hover}"
    textColor: "{colors.paper}"
  button-secondary:
    backgroundColor: "{colors.paper}"
    textColor: "{colors.ink}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.none}"
    padding: "8px 16px"
  button-secondary-hover:
    backgroundColor: "{colors.ground}"
    textColor: "{colors.ink}"
  label:
    backgroundColor: "{colors.paper}"
    textColor: "{colors.ink}"
    rounded: "{rounded.none}"
    padding: "12px 16px 16px"
  tab:
    textColor: "{colors.mute}"
    typography: "{typography.body}"
    padding: "8px 0"
  tab-selected:
    textColor: "{colors.ink}"
  nav-link:
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    padding: "8px 0"
  nav-link-hover:
    backgroundColor: "{colors.ground}"
  segmented-toggle:
    backgroundColor: "{colors.paper}"
    textColor: "{colors.ink}"
    rounded: "{rounded.none}"
    height: "36px"
  segmented-toggle-pressed:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.paper}"
---

# Design System: Dietiary

## Overview

**Creative North Star: "The Nutrition Facts Label"**

Every surface is a printed label: white paper laid on a neutral grey counter, framed by a hairline and ranked from inside by black rules of four weights. The heaviest rule says "this is the headline fact"; the thinnest separates one line item from the next. Hierarchy comes from rule weight and type weight, never from colour blocks, card elevation or icons. The result reads as a document a dietitian can hold up to a client, not a dashboard.

Density is high and calm. Rows are tight (4px to 8px vertical padding), figures are tabular and right-aligned, units sit in muted grey beside the number, and indented sub-rows (fat mass under body fat) carry the label's own nesting. Colour is withheld almost entirely: ink on paper, with exactly two semantic hues that answer one question, "did this move toward the client's goal or away from it?"

The system is bilingual by construction. Every figure passes through locale formatting (English: `74.1`, `30.1%`; Turkish: `74,1`, `%30,1`, dot thousands separator), so layouts must survive both string lengths and both number shapes.

**Key Characteristics:**
- White paper labels on a light grey ground; one hairline frame per label.
- Four ranked ink rules (10, 5, 3, 1px) carry all hierarchy inside a label.
- One heavy grotesk (Libre Franklin 400/700/900) with tabular figures everywhere numbers appear.
- Square corners, no shadows, no gradients, no icons.
- Green and red are reserved for goal direction; nothing else is coloured.

## Colors

An ink-and-paper palette with two goal-direction signals and nothing decorative.

### Primary
- **Label Ink** (#0b0b0b): text, every rule, label frames, the filled primary button, pressed toggle segments, "eaten" adherence cells, chart line and axis. It is the only fill colour for state.
- **Pressed Ink** (#2a2a2a): hover state of ink-filled controls only.

### Secondary
- **Toward Green** (#1a7f37): a change value that moved in the direction of the client's goal (weight down while losing, water up). Text only.
- **Against Red** (#b42318): a change value that moved away from the goal. Text only.

### Neutral
- **Label Paper** (#ffffff): the face of every label, the rail, secondary buttons.
- **Counter Grey** (#ededed): the page ground behind labels, and the hover wash for rows, nav links and secondary buttons.
- **Unit Grey** (#555555): units, inactive tabs, footnotes, secondary facts, disabled text.
- **Pencil Grey** (#8a8a8a): dashed borders for "not yet" states (an upcoming meal cell). Never text.

### Named Rules
**The Goal Direction Rule.** Green and red mean "toward the client's goal" and "against it", decided per metric from the goal, not from the sign of the number. A loss is green for a client losing weight and red for one gaining. A metric with no goal direction (basal metabolic rate) and a zero change stay in ink.

**The Two Hues Rule.** Toward Green and Against Red are the only chromatic colours in the system and appear only as text on change values. No coloured fills, badges, borders, links or brand accents.

## Typography

**Display Font:** Libre Franklin (with Helvetica Neue, Arial)
**Body Font:** Libre Franklin
**Label/Mono Font:** none; figures use Libre Franklin's tabular numerals.

**Character:** One American grotesk at three weights, as on the printed panel it borrows from: black 900 for names and fact headers, bold 700 for column heads and values, regular 400 for line items.

### Hierarchy
- **Display** (900, 38px, 1.05, -0.02em; 30px under 820px): the client's name, once per screen.
- **Headline** (900, 30px, 1.1, -0.01em; 26px under 820px): the primary label's title ("Measurement facts").
- **Title** (900, 22px, 1.15): secondary label titles (Visits, Plan adherence, Goal and plan); the wordmark uses the same size at -0.02em.
- **Figure Lead** (900, 20px; 17px under 820px): the lead row of a facts label (Weight). The lead line of a key-value label steps down to 18px.
- **Body** (400, 14px, 1.45): line items, nav, tabs. Base size of the system.
- **Body Strong** (700, 14px): values in key-value rows, buttons, emphasised facts.
- **Aside** (400, 13px): the right-hand note in a label head, the sub-line under a title, the account block.
- **Label** (700, 12px): column heads, table heads, weekday heads of the adherence grid.
- **Footnote** (400, 12px, Unit Grey): sample-data footnotes and legends. Chart axis numerals drop to 11px.

### Named Rules
**The Tabular Figures Rule.** Every number that can sit above or beside another number uses tabular figures (`font-variant-numeric: tabular-nums`) and right alignment, so columns of values line up by digit in both locales.

**The Weight Not Size Rule.** Hierarchy inside a label is made with 900 vs 700 vs 400 and with rule weight. Sizes stay in the 12 to 22px band inside labels; only the client name and the primary label title go larger.

## Layout

A two-column app shell: a 220px navigation rail (itself a paper label with a 3px right rule) and a main column capped at 1220px with 24px top and 32px side padding. The client header ends in a 3px rule; tabs sit beneath on a 1px rule.

The overview is a two-column label grid: the primary facts label at a fixed 520px, the remaining labels in a flexible column, 20px row gap and 24px column gap, top-aligned. Single-subject panels (a history table, goal list) cap at 760px.

Spacing runs on a 4px base (4, 8, 12, 16, 20, 24, 32, 48). Rows inside labels use 4px or 8px vertical padding; indented sub-rows step in 16px.

Responsive behaviour: under 1100px labels stack into one column in reading priority (measurement, adherence, goal, visits, weight). Under 820px the rail becomes a top bar with wrapping nav links and the language toggle top-right, the wordmark rule drops from 10px to 5px, the facts line loses its dividers, numeric columns narrow, and low-priority table columns hide.

## Elevation & Depth

The system is flat. There are no shadows anywhere. Depth is two planes only: grey ground and white paper, separated by a 1px ink frame. Everything else is ranking within the paper, done with rules.

### Named Rules
**The Ranked Rules Rule.** Rules carry hierarchy at four weights. 10px marks the one headline break in a screen's primary facts label (under its sub-line) and the wordmark, nothing else. 5px closes a label head and the lead row, and underlines the selected tab. 3px separates sections: column heads, section rows (Circumferences, Calculated), the client header, the rail. 1px separates line items and frames each label. Controls use a separate 2px stroke that is not part of the rule ranking.

**The Paper Not Cards Rule.** Containers are labels: white, 1px ink frame, square. Never a shadowed or tinted card.

## Shapes

Every corner is square (0px). Form comes from straight rules, hairline frames and solid or outlined rectangles: the adherence grid is a field of 24px-tall rectangles, filled ink for eaten, outlined for not logged, dashed Pencil Grey for later today. The only non-rectangular marks are the weight chart's line (2.5px ink) and its open circular points. Footnoted values carry a bold dagger (†) superscript, as on a printed label.

## Components

### Buttons
- **Shape:** square (0px), 2px ink stroke, 8px 16px padding, Body Strong.
- **Primary:** ink fill, paper text. One per header ("Add measurement").
- **Secondary:** paper fill, ink text and stroke.
- **Hover / Focus:** hover washes secondary to Counter Grey and primary to Pressed Ink, over 160ms on the standard ease; press nudges down 1px. Focus is a 2px ink outline at 2px offset.
- **Disabled:** secondary turns to a dashed Unit Grey stroke and text; primary turns to a Counter Grey block with Unit Grey text.

### Label (signature component)
The one container of the system.
- **Corner Style:** square.
- **Background:** Label Paper on Counter Grey.
- **Border:** 1px ink frame.
- **Internal Padding:** 12px top, 16px sides and bottom.
- **Head:** a title (Headline for the primary label, Title otherwise) with an optional right-aligned Aside, closed by a 5px rule. The primary facts label adds a sub-line of facts closed by the 10px rule.
- **Facts rows:** a four-column grid (item, this visit, previous, change), numbers right-aligned and tabular, units in Unit Grey, 1px rules between rows, 3px under column heads and section rows, 5px under the lead row. Sub-items indent 16px.
- **Key-value rows:** name left, bold value right, 8px vertical padding, 1px rules; the lead row is 900 at 18px over a 3px rule; the last row has no rule.
- **Tables:** 12px bold right-aligned heads over a 3px rule, 1px row rules, Counter Grey row hover.
- **Footnote:** 12px Unit Grey line led by the dagger mark.

### Tabs
- **Style:** text-only tabs, 24px apart, on a 1px rule. Inactive in Unit Grey at 400.
- **Selected:** ink, 900, with a 5px ink underline sitting on the rule.
- **Keyboard:** arrow keys move between tabs (roving tabindex).

### Navigation
- **Style:** the rail is a paper label. Wordmark in 900 at 22px over the 10px rule; links are full-width rows with 1px ink rules and an optional right-aligned count.
- **States:** hover washes Counter Grey; current page is 900.
- **Mobile:** a wrapping horizontal row of links without rules or counts.

### Segmented Toggle
- **Style:** the EN/TR language switch. Two equal square segments in a 2px ink stroke, 36px minimum height, 700 at 13px.
- **Pressed:** ink fill, paper text.

### Adherence Grid
A meal-by-day matrix: a 96px meal-name column and seven equal day columns with 4px gaps. Cells are 24px-tall rectangles with a 1px ink stroke, filled ink when eaten, empty when not logged, dashed Pencil Grey when still to come. Today's weekday head is underlined 2px. A legend of 12px swatches follows.

### Weight Chart
An inline SVG on paper: 2.5px ink line, open paper points with 2px ink stroke (the latest point larger), faint ink gridlines at 18% opacity, a 3px ink baseline, and the goal as a 1px dashed ink line labelled in 12px bold.

## Do's and Don'ts

### Do:
- **Do** put every group of numbers inside a paper label with a 1px ink frame and rank it with the 10/5/3/1px rules.
- **Do** show each value next to its previous value and colour only the change, Toward Green or Against Red, by the client's goal direction.
- **Do** use tabular figures and right alignment for every numeric column, and format numbers per locale (Turkish decimal comma, dot thousands separator, leading % sign).
- **Do** keep units in Unit Grey at 400 beside the figure.
- **Do** mark not-yet-stored or sample values with a bold dagger and a Unit Grey footnote in the same label.
- **Do** use ink as the only fill for state (primary button, pressed toggle, eaten cell) and Counter Grey as the only hover wash.

### Don't:
- **Don't** use rounded corners, shadows, gradients or tinted cards.
- **Don't** use the 10px rule more than once in a label, or anywhere other than the primary facts label and the wordmark.
- **Don't** use green or red for anything other than goal-direction change values: not for buttons, links, badges, status chips or backgrounds.
- **Don't** colour a change by its sign; colour it by the goal.
- **Don't** add icons; labels speak in words, rules and the dagger mark.
- **Don't** introduce a second typeface; weight 900 / 700 / 400 of Libre Franklin covers display through footnote.
