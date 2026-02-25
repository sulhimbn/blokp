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

## Completed Tasks

### Task 1: Issue #358 - Missing contentDescription on ImageViews
- **Date**: 2026-02-25
- **Files Changed**:
  - `app/src/main/res/layout/activity_menu.xml` - Added contentDescription to 4 ImageViews
  - `app/src/main/res/values/strings.xml` - Added 4 new string resources
- **PR**: https://github.com/sulhimbn/blokp/pull/366
- **Labels**: ui-ux-engineer
- **Status**: Open

### Task 2: Fix Merge Conflict Markers in activity_menu.xml
- **Date**: 2026-02-25
- **Issue Found**: Unresolved merge conflict markers (`<<<<<<< HEAD`, `=======`, `>>>>>>> origin/main`) in activity_menu.xml
- **Impact**: Would cause build failure due to invalid XML
- **Files Changed**:
  - `app/src/main/res/layout/activity_menu.xml` - Removed merge conflict markers, kept string resources version
- **PR**: https://github.com/sulhimbn/blokp/pull/387
- **Labels**: ui-ux-engineer
- **Status**: Open

### Task 3: Issue #95 - Extract Hardcoded Strings to Resources
- **Date**: 2026-02-25
- **Issue**: Inconsistent String Resource Management and Hardcoded Text
- **Files Changed**:
  - `app/src/main/res/layout/item_list.xml` - Replaced 6 hardcoded strings with @string/ references
  - `app/src/main/res/layout/activity_dashboard.xml` - Replaced 20+ hardcoded strings
  - `app/src/main/res/layout/activity_menu.xml` - Replaced 1 hardcoded string
  - `app/src/main/res/values/strings.xml` - Added 30+ new string resources
  - `app/src/main/java/.../DashboardActivity.kt` - Updated to use getString() for dynamic text
- **PR**: https://github.com/sulhimbn/blokp/pull/412
- **Labels**: ui-ux-engineer
- **Status**: Open
- **Benefits**:
  - Consistency across the app
  - Accessibility (screen readers can now read labels)
  - Localization support (all strings in one place)


## Notes
- Build verification couldn't run due to missing Android SDK in environment
- XML changes are syntactically correct and follow Android best practices
