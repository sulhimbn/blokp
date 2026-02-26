# UI/UX Engineer - Long-term Memory

## Overview
This document tracks the work, patterns, and learnings of the ui-ux-engineer agent for this project.

## Domain
- UI/UX improvements
- Accessibility (content descriptions, screen reader support)
- Layout improvements
- Visual consistency

## Patterns & Conventions

### Accessibility - contentDescription
When adding accessibility to ImageViews:
1. Use `@string/` references for contentDescription (not hardcoded strings)
2. Name strings consistently: `content_<menu_name>` in Indonesian
3. Add descriptive text that makes sense for screen readers

### String Resources
Location: `app/src/main/res/values/strings.xml`
- Group accessibility strings with a comment: `<!-- Accessibility content descriptions for menu icons -->`
- Use descriptive, user-friendly text in Indonesian

### Layout Files
Location: `app/src/main/res/layout/`
- XML attributes should be properly formatted
- Use consistent indentation
- Use vector drawables instead of system drawables for consistency

### Vector Drawables
Location: `app/src/main/res/drawable/`
- Use `app:srcCompat` to reference vector drawables in XML
- Vector drawables should use `tint` attribute to match app theme color (@color/teal_200)
- Avoid using Android system drawables (@android:drawable/*) for app icons

## Completed Tasks

### Task 6: Extract hardcoded strings from activity_dashboard.xml
- **Date**: 2026-02-26
- **Issue Found**: 21 hardcoded strings in activity_dashboard.xml
- **Impact**: Improved localization support and consistency with other layout files
- **Files Changed**:
  - `app/src/main/res/layout/activity_dashboard.xml` - Changed 21 strings to @string references
  - `app/src/main/res/values/strings.xml` - Added 7 new string resources
- **New Strings Added**: users_label, reports_label, pay_label, payment_status_label, payment_status_good, residents_paid_status, last_synced_never_display
- **Labels**: ui-ux-engineer
- **Status**: Completed (PR #503)

### Task 5: Fix missing/inconsistent menu icons in activity_menu.xml
- **Date**: 2026-02-26
- **Issue**: Menu icons were using non-existent drawables (@drawable/profile, @drawable/kas) and using wrong icons for different menus (profile used for both warga and komunikasi)
- **Changes Made**:
  - Created `ic_menu_warga.xml` - Person/user icon vector drawable
  - Created `ic_menu_laporan.xml` - Document/report icon vector drawable
  - Created `ic_menu_komunikasi.xml` - Chat/communication icon vector drawable
  - Created `ic_menu_pembayaran.xml` - Money/payment icon vector drawable
  - Updated `activity_menu.xml` to use new icons:
    - imageView1: @drawable/ic_menu_warga (was profile)
    - imageView2: @drawable/ic_menu_laporan (was kas)
    - imageView3: @drawable/ic_menu_komunikasi (was profile)
    - imageView4: @drawable/ic_menu_pembayaran (was kas)
- **Impact**: Fixed missing icon resources and improved visual consistency with proper themed icons
- **Labels**: ui-ux-engineer
- **Status**: Completed

### Task 4: Fix deprecated margin attributes and textSize units in activity_menu.xml
- **Date**: 2026-02-25
- **Issue**: #458
- **Changes Made**:
  - Replaced deprecated `android:layout_marginLeft` with `android:layout_marginStart` (lines 13, 33, 94)
  - Replaced deprecated `android:layout_marginRight` with `android:layout_marginEnd` (lines 15, 103)
  - Fixed `android:textSize="15dp"` to `android:textSize="15sp"` (lines 57, 84, 121, 149)
- **Impact**: Improved RTL layout support and proper text scaling
- **Labels**: ui-ux-engineer
- **Status**: Completed

### Task 3: Hardcoded contentDescription in item_list.xml
- **Date**: 2026-02-25
- **Issue Found**: Hardcoded `android:contentDescription="Avatar"` in item_list.xml
- **Impact**: Accessibility issue - screen readers won't read proper description
- **Files Changed**:
  - `app/src/main/res/layout/item_list.xml` - Changed to @string/content_avatar
  - `app/src/main/res/values/strings.xml` - Added content_avatar string resource
- **Labels**: ui-ux-engineer
- **Status**: Completed

### Task 2: Fix Merge Conflict Markers in activity_menu.xml
- **Date**: 2026-02-25
- **Issue Found**: Unresolved merge conflict markers (`<<<<<<< HEAD`, `=======`, `>>>>>>> origin/main`) in activity_menu.xml
- **Impact**: Would cause build failure due to invalid XML
- **Files Changed**:
  - `app/src/main/res/layout/activity_menu.xml` - Removed merge conflict markers, kept string resources version
- **Labels**: ui-ux-engineer
- **Status**: Completed

### Task 1: Issue #358 - Missing contentDescription on ImageViews
- **Date**: 2026-02-25
- **Files Changed**:
  - `app/src/main/res/layout/activity_menu.xml` - Added contentDescription to 4 ImageViews
  - `app/src/main/res/values/strings.xml` - Added 4 new string resources
- **Labels**: ui-ux-engineer
- **Status**: Completed

## Notes
- Build verification couldn't run due to missing Android SDK in environment
- XML changes are syntactically correct and follow Android best practices
- All vector drawables use @color/teal_200 for tint to match app theme
