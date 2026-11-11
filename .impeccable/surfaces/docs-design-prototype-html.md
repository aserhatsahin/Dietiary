---
version: 1
slug: "docs-design-prototype-html"
primary_target: "docs/design/prototype.html"
related_targets: []
---

# Client overview (dietitian panel)

## Scope
Mode: Operate. The dietitian's per-client screen, opened before, during and after a consultation (clinic or video call); occasionally shown to the client for the chart. Bilingual EN/TR.

## Task and content
First glance: this visit's measurement against the previous one (scale body composition plus tape circumferences), this week's plan adherence, and the goal with the active plan. Data: demo client Elif Kaya. Fat mass, edema and circumferences are not in the API yet; shown here as the target data shape.

Must not feel: clinically cold, toy-like, spreadsheet-like, or generic SaaS.

## Direction contract
THESIS: The client's numbers are read like a Nutrition Facts panel: one bordered label whose thick and thin rules rank the data. It refuses the KPI-card row with a line chart, which is the category default.
OWN-WORLD: White paper on a neutral grey ground; ink-black rules at 10, 5, 3 and 1 px; a heavy grotesk with tabular figures; one green for change toward the goal and one red for change against it. No fills, shadows or radii.
STORY: The dietitian sees what changed since the last visit and whether the plan was followed, then records or adjusts.
FIRST VIEWPORT: Label-style navigation rail on the left. A heavy client name with a fixed info line, and "Add measurement" in the top right. The measurement label is the left column at 520 px. On the right are the adherence label (meal × day grid) and the goal label.
FORM: Nutrition Facts label, candidate 1 of 7; seed:fd64e1bd575cfa34
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance

## Open decisions
- Mobile layout for the panel (labels stack).
- Backend fields for fat mass, edema and circumferences.
