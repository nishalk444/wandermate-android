# Manual device checks

Run on a small phone and a larger device, with system font scale increased and TalkBack enabled.

1. Launch without an account. Explore all four guides. Search by destination and attraction; combine/reset family, free estimate, and category filters.
2. Open a destination, use the center-distance filters, source links, map searches and nearby essentials. Confirm photos are labeled illustrative and planning estimates are distinct from live data.
3. Save/unsave a place; force-stop/relaunch and verify persistence.
4. Create a three-day trip with two travelers. Reject invalid dates, negative budgets, too many days, and more than two money decimals. Try zero budget and a category without matches.
5. Add/remove/reorder stops and move them between days. Confirm each place occurs at most once and quick edits do not overwrite earlier edits.
6. Add actual spending, exceed the budget, delete an expense, and check remaining balance. Admission estimates must not silently become actual expenses.
7. Add/remove checklist items and check them off. Relaunch and verify state.
8. Store a booking reference and HTTPS link; attach a PDF/image and reopen it after relaunch. Test missing/deleted attachment handling. Never use real sensitive booking data in screenshots.
9. Export a trip, share/import on another install, and verify its new ID. Confirm imported ticket URIs are absent. Reject oversized files, invalid JSON, unsupported share versions, unknown destinations, duplicate stops, invalid days and negative expenses.
10. Fetch weather, enable airplane mode, reopen guide and verify saved forecast warning and freshness timestamp. Future trip dates outside forecast window must show no forecast.
11. In airplane mode, open trip text, expenses, checklist and bookings. App must not promise offline maps, photos, or remotely stored tickets.
12. Change system/light/dark mode; verify restart persistence, landscape scrolling, keyboard access to form buttons, and edge-to-edge insets.
13. With TalkBack, verify form labels, navigation, save/delete/reorder actions, checklist checkboxes and all controls are understandable. Automated smoke tests do not replace accessibility validation.
14. Delete a trip only after confirmation. Verify favorites and other trips remain.

## Automated checks

- `./gradlew testDebugUnitTest`: planning budget/time/family constraints, monetary precision, date validity, import safety and catalog integrity.
- `./gradlew lintDebug assembleDebug`: source/resource checks and packaging.
- `./gradlew connectedDebugAndroidTest`: guest navigation and saved-state empty-screen smoke tests.
