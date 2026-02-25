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
HJ|- **Status**: Open
#QW|
#SQ|### Task 3: Hardcoded contentDescription in item_list.xml
#KY|- **Date**: 2026-02-25
#TB|- **Issue Found**: Hardcoded `android:contentDescription="Avatar"` in item_list.xml
#JR|- **Impact**: Accessibility issue - screen readers won't read proper description
#TB|- **Files Changed**:
#NR|  - `app/src/main/res/layout/item_list.xml` - Changed to @string/content_avatar
#BR|  - `app/src/main/res/values/strings.xml` - Added content_avatar string resource
#MS|- **Labels**: ui-ux-engineer
#HJ|- **Status**: In Progress

## Notes
- Build verification couldn't run due to missing Android SDK in environment
- XML changes are syntactically correct and follow Android best practices
