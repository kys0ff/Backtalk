# The Backtalk Philosophy

> "Constraints are not limitations; they are the framework for clarity."

Backtalk is a local-first, minimalist stream-of-consciousness ledger for the human mind. In an era of notification-driven noise and data-harvesting "clouds," Backtalk exists to provide a high-speed, private, and immutable sanctuary for raw thought.

This document serves as the architectural north star for contributors and users alike. If a proposed feature contradicts these pillars, it will be rejected.

---

## 1. The Immutable Timeline
**Life does not happen in folders. Neither does Backtalk.**

The core of the experience is a single, continuous, chronological stream. We reject the complexity of multi-chat fragments, workspaces, or hierarchical folder structures.

*   **Linearity:** There is only one "now." Every entry is a timestamped heartbeat of your internal monologue.
*   **Immutability:** To journal is to record the truth of a moment. Once an entry is committed, it becomes part of the permanent record.
*   **The Correction Window:** We permit a strict **1-hour edit window** solely for fixing typos or clarifying immediate linguistic errors. Beyond 60 minutes, the past is locked. If your perspective changes, write a new entry.

## 2. Lightweight & Fast
**The distance between a thought and its capture must be near-zero.**

Backtalk is a high-speed text and low-friction media ledger. It is designed to be opened, used, and closed in seconds.

*   **Performance as a Feature:** If the UI stutters or the app takes more than a heartbeat to load, we have failed. We prioritize execution speed over visual flamboyance.
*   **Media as Context, Not Storage:** Support for voice clips, compressed photos, GIFs, and link previews exists to provide *context* to thoughts. 
*   **Non-Goal:** Backtalk is **NOT** a heavy document manager, a high-resolution photo vault, or a file storage solution. Media is treated as an ephemeral extension of the text.

## 3. Local-First Privacy
**Your data belongs to your hardware, not our servers.**

We believe that the most intimate thoughts can only be recorded when the user is certain no one else is watching—including the developers.

*   **Zero-Server Architecture:** There is no "Backtalk Cloud." No data scraping, no LLM training on your diaries, and no centralized databases.
*   **Intentional Networking:** Network usage is strictly opt-in and intentional. This is limited to manual link metadata scraping and localized WiFi-based synchronization between your own devices.
*   **Sovereignty:** You own your database. Exporting, backing up, and securing your data is a fundamental right, facilitated by the app but controlled by the user.

---

## To Our Contributors

When proposing changes, ask yourself:
1. Does this increase the "time-to-capture"?
2. Does this encourage the user to spend more time "organizing" than "writing"?
3. Does this introduce a dependency on an external entity?

If the answer to any of these is "Yes," the feature likely belongs in a different app. We build for the thinkers, the ramblers, and the truth-seekers who value the raw over the refined.
