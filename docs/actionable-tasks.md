# Iuran BlokP - Actionable Task List

## Critical Tasks (Week 1-2)

### 1. Language Migration - MenuActivity.java → Kotlin
**Issue**: #154  
**Estimated Time**: 4-6 hours  
**Assignee**: Android Developer  
**Dependencies**: None

#### Subtasks:
- [ ] Convert MenuActivity.java to Kotlin syntax
- [ ] Implement ViewBinding for activity_menu.xml
- [ ] Replace findViewById with binding references
- [ ] Update click listeners to Kotlin lambda syntax
- [ ] Test navigation flows to MainActivity and LaporanActivity
- [ ] Remove MenuActivity.java file
- [ ] Update AndroidManifest.xml if needed
- [ ] Run full test suite to ensure no regressions

#### Acceptance Criteria:
- Menu functionality identical to current implementation
- No Java files remain in codebase
- ViewBinding properly configured
- All navigation flows work correctly

---

### 2. Fix Critical Financial Calculation Bug
**Issue**: #18  
**Estimated Time**: 2-3 hours  
**Assignee**: Android Developer  
**Dependencies**: None

#### Subtasks:
- [ ] Review calculation logic in LaporanActivity.kt:76
- [ ] Identify magic number `* 3` and create constant
- [ ] Add input validation for negative values
- [ ] Create unit tests for calculation scenarios
- [ ] Test with various data combinations
- [ ] Update documentation for calculation formula

#### Acceptance Criteria:
- Financial calculations mathematically correct
- All edge cases handled (negative values, zero values)
- Unit tests cover all calculation scenarios
- Formula documented and constants defined

---

### 3. Implement BaseActivity for Common Functionality
**Issue**: #110  
**Estimated Time**: 6-8 hours  
**Assignee**: Android Developer  
**Dependencies**: None

#### Subtasks:
- [ ] Create BaseActivity abstract class
- [ ] Extract common retry logic from MainActivity and LaporanActivity
- [ ] Implement generic error handling
- [ ] Add network connectivity checking
- [ ] Create common loading states
- [ ] Refactor MainActivity to extend BaseActivity
- [ ] Refactor LaporanActivity to extend BaseActivity
- [ ] Test retry mechanisms and error handling

#### Acceptance Criteria:
- Code duplication eliminated
- Consistent error handling across activities
- Retry logic centralized and configurable
- Network checks standardized

---

## High Priority Tasks (Week 2-4)

### 4. MVVM Architecture Implementation
**Issue**: #155  
**Estimated Time**: 16-20 hours  
**Assignee**: Android Lead + Developer  
**Dependencies**: Task #3 completed

#### Subtasks:
- [ ] Add Hilt dependency injection
- [ ] Create Repository interfaces and implementations
- [ ] Implement UserRepository with API integration
- [ ] Implement FinancialRepository for calculations
- [ ] Create UserViewModel with LiveData/StateFlow
- [ ] Create LaporanViewModel with business logic
- [ ] Refactor MainActivity to use UserViewModel
- [ ] Refactor LaporanActivity to use LaporanViewModel
- [ ] Add comprehensive unit tests for ViewModels
- [ ] Update error handling to use ViewModel

#### Acceptance Criteria:
- Clear separation of concerns achieved
- All business logic moved to ViewModels
- Activities only handle UI logic
- Comprehensive test coverage for ViewModels
- Dependency injection properly configured

---

### 5. Comprehensive Testing Strategy
**Issue**: #157  
**Estimated Time**: 12-16 hours  
**Assignee**: QA Engineer + Android Developer  
**Dependencies**: Task #4 completed

#### Subtasks:
- [ ] Setup test dependencies (Mockito, Espresso, MockWebServer)
- [ ] Create unit tests for all ViewModels
- [ ] Create unit tests for Repository implementations
- [ ] Create unit tests for utility classes (NetworkUtils)
- [ ] Create integration tests for API layer
- [ ] Create UI tests for MainActivity with Espresso
- [ ] Create UI tests for LaporanActivity with Espresso
- [ ] Create UI tests for MenuActivity navigation
- [ ] Setup test coverage reporting (JaCoCo)
- [ ] Configure automated testing in GitHub Actions
- [ ] Document testing guidelines and best practices

#### Acceptance Criteria:
- Minimum 80% test coverage for critical components
- All unit tests pass consistently
- Integration tests cover API scenarios
- UI tests validate user flows
- Automated testing pipeline functional

---

### 6. Dependency Management Cleanup
**Issue**: #156  
**Estimated Time**: 4-6 hours  
**Assignee**: Android Developer  
**Dependencies**: None

#### Subtasks:
- [ ] Audit all dependencies in build.gradle files
- [ ] Remove unused android-async-http dependency
- [ ] Update core-ktx from 1.7.0 to 1.12.0
- [ ] Update Android Gradle Plugin to 8.1.0
- [ ] Create version catalog (libs.versions.toml)
- [ ] Migrate all dependencies to version catalog
- [ ] Test build process after updates
- [ ] Update documentation for dependency management

#### Acceptance Criteria:
- No unused dependencies remain
- All dependencies updated to stable versions
- Version catalog properly implemented
- Build process successful
- Documentation updated

---

## Medium Priority Tasks (Week 4-8)

### 7. ViewBinding Migration
**Issue**: #108  
**Estimated Time**: 8-10 hours  
**Assignee**: Android Developer  
**Dependencies**: Task #1 completed

#### Subtasks:
- [ ] Enable ViewBinding in build.gradle
- [ ] Create binding classes for all layouts
- [ ] Replace findViewById in MainActivity
- [ ] Replace findViewById in LaporanActivity
- [ ] Update adapter classes to use ViewBinding
- [ ] Test all UI interactions
- [ ] Remove old findViewById code
- [ ] Update coding standards documentation

#### Acceptance Criteria:
- All findViewById calls replaced with ViewBinding
- Type safety improved for view references
- No runtime errors from view binding
- Code readability improved

---

### 8. Input Validation & Security Hardening
**Issue**: #111  
**Estimated Time**: 6-8 hours  
**Assignee**: Android Developer  
**Dependencies**: Task #4 completed

#### Subtasks:
- [ ] Create validation utility classes
- [ ] Add input sanitization for API responses
- [ ] Implement validation for user input fields
- [ ] Add SQL injection prevention
- [ ] Implement XSS protection for web views
- [ ] Add certificate pinning validation
- [ ] Create security tests
- [ ] Update security documentation

#### Acceptance Criteria:
- All user inputs validated and sanitized
- API responses validated before processing
- Security tests pass
- Certificate pinning functional
- Security guidelines documented

---

### 9. Progress Indicators & UX Improvements
**Issue**: #94  
**Estimated Time**: 8-10 hours  
**Assignee**: Android Developer  
**Dependencies**: Task #4 completed

#### Subtasks:
- [ ] Add loading indicators for API calls
- [ ] Implement swipe-to-refresh functionality
- [ ] Add empty state designs
- [ ] Create error state layouts
- [ ] Implement smooth transitions between states
- [ ] Add haptic feedback for user interactions
- [ ] Optimize image loading with placeholders
- [ ] Test UX improvements on various devices

#### Acceptance Criteria:
- Clear feedback during loading states
- Smooth transitions between UI states
- Professional error and empty states
- Consistent user experience across app

---

## Low Priority Tasks (Week 8-12)

### 10. String Resource Management
**Issue**: #95  
**Estimated Time**: 4-6 hours  
**Assignee**: Android Developer  
**Dependencies**: None

#### Subtasks:
- [ ] Identify all hardcoded strings in code
- [ ] Create string resources for all text
- [ ] Implement string formatting for dynamic content
- [ ] Add string resources for error messages
- [ ] Create string resources for success messages
- [ ] Update all hardcoded string references
- [ ] Test with different locales (if applicable)

#### Acceptance Criteria:
- No hardcoded strings remain in code
- All text properly externalized
- Easy localization support
- Consistent messaging across app

---

### 11. Image Caching Strategy
**Issue**: #115  
**Estimated Time**: 6-8 hours  
**Assignee**: Android Developer  
**Dependencies**: None

#### Subtasks:
- [ ] Configure Glide caching strategy
- [ ] Implement memory caching for avatars
- [ ] Add disk caching for offline support
- [ ] Create image loading utilities
- [ ] Add placeholder and error images
- [ ] Test caching behavior
- [ ] Optimize image sizes and formats

#### Acceptance Criteria:
- Images load faster on subsequent views
- Offline image viewing functional
- Memory usage optimized
- Professional loading and error states

---

### 12. Documentation Updates
**Issue**: #50  
**Estimated Time**: 8-10 hours  
**Assignee**: Android Lead + Technical Writer  
**Dependencies**: All major tasks completed

#### Subtasks:
- [ ] Update API documentation
- [ ] Create architecture documentation
- [ ] Document MVVM implementation patterns
- [ ] Update setup and deployment guides
- [ ] Create troubleshooting documentation
- [ ] Document testing procedures
- [ ] Update README with current features
- [ ] Create contributor guidelines

#### Acceptance Criteria:
- All documentation up-to-date
- Clear architecture diagrams
- Comprehensive setup guides
- Easy-to-follow contribution process

---

## Task Dependencies Graph

```
Week 1-2 (Critical):
├── Task #1: Language Migration
├── Task #2: Financial Bug Fix
└── Task #3: BaseActivity Implementation

Week 2-4 (High Priority):
├── Task #4: MVVM Architecture (depends on #3)
├── Task #5: Testing Strategy (depends on #4)
└── Task #6: Dependency Cleanup

Week 4-8 (Medium Priority):
├── Task #7: ViewBinding Migration (depends on #1)
├── Task #8: Security Hardening (depends on #4)
└── Task #9: UX Improvements (depends on #4)

Week 8-12 (Low Priority):
├── Task #10: String Resources
├── Task #11: Image Caching
└── Task #12: Documentation (depends on all major tasks)
```

## Resource Allocation

### Team Capacity (per week):
- **Android Lead**: 40 hours (architecture oversight, code review)
- **Android Developer**: 40 hours (feature implementation, bug fixes)
- **QA Engineer**: 40 hours (testing strategy, test automation)
- **Technical Writer**: 20 hours (documentation, guides)

### Total Estimated Effort:
- **Critical Tasks**: 12-17 hours
- **High Priority Tasks**: 32-42 hours
- **Medium Priority Tasks**: 22-28 hours
- **Low Priority Tasks**: 18-24 hours
- **Total**: 84-111 hours across 12 weeks

## Success Metrics

### Technical Metrics:
- [ ] 0 Java files remaining
- [ ] 80%+ test coverage
- [ ] 0 critical security vulnerabilities
- [ ] < 2 second app startup time
- [ ] 0 code duplication in retry logic

### Quality Metrics:
- [ ] All PRs pass automated tests
- [ ] Code review coverage 100%
- [ ] Documentation completeness 90%+
- [ ] Crash rate < 0.1%

---

*Last Updated: November 2025*
*Next Review: Weekly standup meetings*
*Task List Owner: Development Team Lead*