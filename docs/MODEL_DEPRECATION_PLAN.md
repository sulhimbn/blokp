# Model Directory Deprecation Plan

## Overview
This document outlines the deprecation plan for the `model/` directory, which currently contains a mix of Data Transfer Objects (DTOs) and domain-like models. The goal is to migrate to a cleaner architecture with clear separation of concerns.

## Current State

### model/ Directory Contents
The `model/` directory contains the following files:

1. **DataItem.kt** - Legacy DTO for user/financial data
2. **ValidatedDataItem.kt** - Validated version of DataItem
3. **Announcement.kt** - Announcement model (domain-like)
4. **CommunityPost.kt** - Community post model (domain-like)
5. **Message.kt** - Message model (domain-like)
6. **PaymentModels.kt** - Payment-related models (DTOs)
7. **Vendor.kt** - Vendor model (domain-like)
8. **VendorModels.kt** - Vendor-related models (DTOs)
9. **WorkOrder.kt** - Work order model (domain-like)

### Current Usage
The `model/` directory is currently used in:
- **Presentation Layer**: ViewModels and Adapters import from `model/`
- **Data Layer**: EntityMapper converts to/from `model/` classes

### Issues with Current Structure
1. **Mixed Concerns**: Mix of DTOs and domain models in same directory
2. **Architectural Inconsistency**: No clear separation between data and domain layers
3. **Confusion**: Unclear which models to use (DataItem vs Entity vs Domain Model)
4. **Maintainability**: Difficult to understand data flow and responsibilities

## Target Architecture

### New Directory Structure
After migration, the architecture will be:

```
app/src/main/java/com/example/iurankomplek/
├── domain/
│   ├── model/
│   │   ├── User.kt ✅ (domain model)
│   │   ├── FinancialRecord.kt ✅ (domain model)
│   │   ├── Announcement.kt (move from model/)
│   │   ├── CommunityPost.kt (move from model/)
│   │   ├── Message.kt (move from model/)
│   │   ├── Vendor.kt (move from model/)
│   │   └── WorkOrder.kt (move from model/)
│   └── usecase/ (future - use case implementations)
├── data/
│   ├── entity/
│   │   ├── UserEntity.kt ✅ (Room entity)
│   │   ├── FinancialRecordEntity.kt ✅ (Room entity)
│   │   └── Transaction.kt ✅ (Room entity)
│   ├── dto/
│   │   ├── UserDto.kt ✅ (API DTO)
│   │   ├── FinancialDto.kt ✅ (API DTO)
│   │   ├── PaymentModels.kt (move from model/)
│   │   └── VendorModels.kt (move from model/)
│   └── mapper/
│       ├── EntityMapper.kt ✅ (entity ↔ DTO)
│       └── DomainMapper.kt ✅ (entity ↔ domain model)
└── model/ (DEPRECATED - will be removed)
```

## Migration Plan

### Phase 1: Domain Model Creation (✅ COMPLETED - Module 48)
**Date**: 2026-01-08
**Status**: Completed

**Tasks Completed**:
- [x] Create `domain/model/` directory
- [x] Create User.kt domain model
- [x] Create FinancialRecord.kt domain model
- [x] Create DomainMapper.kt for entity ↔ domain model conversion
- [x] Document domain layer architecture in blueprint.md

**Impact**: Foundation laid for domain layer, no breaking changes to existing code

### Phase 2: Domain Model Expansion (PLANNED)
**Priority**: Medium
**Estimated Time**: 2-3 hours

**Tasks**:
- [ ] Move domain-like models from `model/` to `domain/model/`
  - Announcement.kt → domain/model/Announcement.kt
  - CommunityPost.kt → domain/model/CommunityPost.kt
  - Message.kt → domain/model/Message.kt
  - Vendor.kt → domain/model/Vendor.kt
  - WorkOrder.kt → domain/model/WorkOrder.kt
- [ ] Add validation logic to moved domain models
- [ ] Create DomainMapper extensions for moved models
- [ ] Update imports in affected files
- [ ] Verify no compilation errors

**Impact**: Domain layer expanded to cover all business entities, no breaking changes

### Phase 3: DTO Migration (PLANNED)
**Priority**: Low
**Estimated Time**: 1-2 hours

**Tasks**:
- [ ] Move DTOs from `model/` to `data/dto/`
  - PaymentModels.kt → data/dto/PaymentModels.kt
  - VendorModels.kt → data/dto/VendorModels.kt
- [ ] Update imports in affected files
- [ ] Verify no compilation errors

**Impact**: DTOs properly placed in data layer, no breaking changes

### Phase 4: Legacy DTO Replacement (PLANNED)
**Priority**: High
**Estimated Time**: 4-6 hours

**Tasks**:
- [ ] Update EntityMapper to use DomainMapper for entity → domain model conversion
- [ ] Update Repository interfaces to return domain models
- [ ] Update Repository implementations to convert Entity → Domain Model
- [ ] Update ViewModels to consume domain models
- [ ] Update Adapters to work with domain models
- [ ] Remove usage of DataItem and ValidatedDataItem from presentation layer
- [ ] Remove EntityMapper's DataItem conversion methods

**Impact**: Major refactoring, domain models used throughout architecture

### Phase 5: Legacy DTO Removal (PLANNED)
**Priority**: Medium
**Estimated Time**: 1-2 hours

**Tasks**:
- [ ] Remove DataItem.kt from `model/`
- [ ] Remove ValidatedDataItem.kt from `model/`
- [ ] Update EntityMapper to remove all DataItem references
- [ ] Update all imports that reference removed DTOs
- [ ] Verify no compilation errors
- [ ] Run full test suite

**Impact**: Legacy DTOs removed, cleaner architecture

### Phase 6: Model Directory Removal (PLANNED)
**Priority**: Low
**Estimated Time**: 0.5 hours

**Tasks**:
- [ ] Verify all files migrated from `model/`
- [ ] Verify no remaining imports from `model/`
- [ ] Remove `model/` directory
- [ ] Update architecture documentation
- [ ] Run full test suite

**Impact**: Model directory removed, architecture fully cleaned up

## Risk Mitigation

### Risks
1. **Breaking Changes**: Major refactoring could break existing functionality
2. **Regression Risk**: Errors introduced during migration
3. **Complexity**: Large codebase refactoring is complex
4. **Timeline**: Migration may take significant time

### Mitigation Strategies
1. **Incremental Migration**: Phase-by-phase approach minimizes risk
2. **No Breaking Changes**: Each phase maintains backward compatibility
3. **Testing**: Comprehensive testing after each phase
4. **Documentation**: Clear documentation for each phase
5. **Code Review**: Thorough review of all changes
6. **Rollback Plan**: Ability to revert changes if needed

## Success Criteria

### Phase 1 ✅ Completed
- [x] domain/model/ directory created
- [x] User.kt domain model created
- [x] FinancialRecord.kt domain model created
- [x] DomainMapper.kt created
- [x] Documentation updated
- [x] No breaking changes to existing code

### Phase 2-6 (Future)
- [ ] All domain models in domain/model/
- [ ] All DTOs in data/dto/
- [ ] model/ directory removed
- [ ] No compilation errors
- [ ] All tests passing
- [ ] No regressions in functionality
- [ ] Documentation updated

## Timeline

| Phase | Description | Priority | Est. Time | Status |
|-------|-------------|------------|------------|---------|
| 1 | Domain Model Creation | HIGH | 1.5 hours | ✅ COMPLETED (2026-01-08) |
| 2 | Domain Model Expansion | MEDIUM | 2-3 hours | 🔄 PLANNED |
| 3 | DTO Migration | LOW | 1-2 hours | 🔄 PLANNED |
| 4 | Legacy DTO Replacement | HIGH | 4-6 hours | 🔄 PLANNED |
| 5 | Legacy DTO Removal | MEDIUM | 1-2 hours | 🔄 PLANNED |
| 6 | Model Directory Removal | LOW | 0.5 hours | 🔄 PLANNED |

**Total Estimated Time**: 10-15 hours (excluding Phase 1 which is completed)

## Dependencies

### Module Dependencies
- **Module 48 (Domain Layer Implementation)** ✅ COMPLETED - Foundation for domain layer
- **Module 49 (LaporanActivity Complexity Reduction)** - High priority, should be done before Phase 4
- **Module 51 (Repository Large Method Extraction)** - Medium priority, should be done before Phase 4

### External Dependencies
- None

## Rollback Plan

If any phase encounters critical issues:
1. **Immediate Rollback**: Revert all changes in the current phase
2. **Assessment**: Evaluate what went wrong and why
3. **Adjustment**: Modify plan based on lessons learned
4. **Retry**: Attempt phase again with adjusted approach

## Conclusion

This deprecation plan provides a clear, phased approach to removing the `model/` directory and achieving a cleaner architecture. The incremental approach minimizes risk and allows for thorough testing at each phase.

**Next Steps**:
1. Complete Module 49 (LaporanActivity Complexity Reduction) - HIGH priority
2. Complete Module 51 (Repository Large Method Extraction) - MEDIUM priority
3. Begin Phase 2: Domain Model Expansion

For questions or concerns about this plan, refer to:
- `docs/blueprint.md` - Architecture documentation
- `docs/task.md` - Task management
- `docs/MODEL_DEPRECATION_PLAN.md` - This document

---

**Document Version**: 1.0
**Last Updated**: 2026-01-08
**Author**: Code Architect
**Status**: Phase 1 Completed, Phases 2-6 Planned
