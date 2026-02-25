# Frontend Engineer Agent - Long-term Memory

## Agent Profile
- **Role**: Frontend Engineer Specialist
- **Domain**: Android UI/UX improvements
- **Project**: BlokP (Iuran community management app)

## Completed Work

### PR #376: Fix fragment lifecycle - ViewModel + viewLifecycleOwner
**Status**: Open
**Date**: 2026-02-25

**Changes Made:**
1. Created ViewModels with StateFlow + UiState pattern:
   - CommunityViewModel.kt
   - MessagesViewModel.kt
   - AnnouncementsViewModel.kt

2. Refactored fragments to use ViewModel + viewLifecycleOwner:
   - CommunityFragment.kt
   - MessagesFragment.kt
   - AnnouncementsFragment.kt

3. Benefits:
   - Prevents context leaks after Fragment destruction
   - Lifecycle-aware UI updates using viewLifecycleOwner
   - Follows existing pattern from VendorDatabaseFragment
   - Uses StateFlow for reactive state management

**Issue**: Closes #352

---

### PR #363: Fix accessibility - ImageView content descriptions and color contrast
**Status**: Open
**Date**: 2026-02-25

**Changes Made:**
1. **activity_menu.xml**: Added contentDescription to 4 ImageViews for screen reader accessibility
   - imageView1: "Menu Warga - Daftar Pengguna"
   - imageView2: "Menu Laporan - Laporan Keuangan"  
   - imageView3: "Menu Komunikasi"
   - imageView4: "Menu Pembayaran"

2. **colors.xml**: Fixed inaccessible green color
   - Changed from `#00FF00` (pure bright green - luminance 1.0) 
   - Changed to `#228B22` (ForestGreen - WCAG compliant)

**Issue**: Closes #358

---

## Workflow
1. **INITIATE**: Scan for frontend issues, check existing PRs, check issues
2. **PLAN**: Create todo list with specific tasks
3. **IMPLEMENT**: Make targeted changes (ViewModels, Fragments, XML)
4. **VERIFY**: Run build (requires Android SDK)
5. **SELF-REVIEW**: Review own changes
6. **SELF-EVOLVE**: Document findings for future improvements
7. **DELIVER**: Create PR linked to issue

## Known Limitations
- Build verification requires Android SDK (not available in current CI environment)
- Code follows established patterns from VendorDatabaseFragment/VendorViewModel

## Repository Patterns Identified
- Mixed Kotlin/Java Android project
- XML layouts in `app/src/main/res/layout/`
- Colors defined in `app/src/main/res/values/colors.xml`
- ViewModels use StateFlow + UiState (Loading/Success/Error)
- Fragments observe state with viewLifecycleOwner
- Adapters extend ListAdapter with DiffUtil

## Areas for Future Improvement
- Add contentDescription to more ImageViews across layouts
- Review color contrast ratios throughout the app
- Add accessibility testing to CI pipeline
- Consider replacing remaining callback-based fragments with ViewModel pattern
