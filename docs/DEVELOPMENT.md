# Development Guidelines

## Coding Standards

### Kotlin Style
- Follow official Kotlin coding conventions (kotlin.code.style=official)
- Use descriptive variable and function names
- Prefer immutable variables (val over var) when possible
- Use expression bodies for simple functions
- Use "is" checks instead of explicit casting when possible

### File Organization
- Place new Kotlin files in `app/src/main/java/com/example/iurankomplek/`
- Keep related functionality grouped by feature
- Use meaningful package names (model, network, utils)

### Naming Conventions
- Class names: PascalCase (e.g., MainActivity, UserAdapter)
- Function names: camelCase (e.g., getUser, onResponse)
- Variable names: camelCase (e.g., rv_users, adapter)
- Constants: UPPER_SNAKE_CASE (e.g., BASE_URL)

## Testing Guidelines

### Unit Tests
- Place tests in `app/src/test/java/com/example/iurankomplek/`
- Use JUnit 4 for basic unit tests
- Follow AAA pattern (Arrange, Act, Assert)
- Test business logic separately from UI

### Instrumented Tests
- Place instrumented tests in `app/src/androidTest/java/com/example/iurankomplek/`
- Use Espresso for UI testing
- Test critical user flows

## Git Workflow

### Branch Naming
- Feature branches: `feature/descriptive-name`
- Bug fixes: `fix/descriptive-name`
- Hotfixes: `hotfix/descriptive-name`

### Commit Messages
- Use present tense: "Add feature" not "Added feature"
- Be descriptive but concise
- Start with a verb: "Add", "Update", "Fix", "Remove", etc.
- Reference issue numbers: "Fix: resolve issue with API calls (#42)"

### Pull Requests
- Keep PRs focused on a single issue or feature
- Include a clear description of changes
- Reference related issues
- Ensure all tests pass before submitting

## Code Review Process

### For Contributors
- Submit well-tested code
- Ensure code follows project conventions
- Address all review comments
- Keep PRs small and focused

### For Reviewers
- Check for adherence to coding standards
- Verify functionality works as expected
- Ensure tests are updated or added
- Look for potential bugs or edge cases

## Release Process

### Versioning
- Follow semantic versioning (MAJOR.MINOR.PATCH)
- Update version in `build.gradle` files
- Create Git tags for releases

### Pre-release Checklist
- [ ] All tests pass
- [ ] Documentation is updated
- [ ] Code is reviewed and approved
- [ ] Changelog is updated
- [ ] APK is tested on target devices

## Common Development Tasks

### Adding a New API Endpoint
1. Update ApiService interface
2. Create specific response model if needed
3. Update activities to use new endpoint
4. Add error handling for new endpoint

### Adding a New Activity
1. Create new activity file in appropriate language (prefer Kotlin)
2. Add layout to `res/layout/`
3. Register in AndroidManifest.xml
4. Add navigation in existing activities if needed

### Adding a New Dependency
1. Add to `app/build.gradle`
2. Ensure dependency is properly licensed
3. Update documentation if needed
4. Test thoroughly

## Performance Considerations
- Avoid heavy operations on the main thread
- Use RecyclerView for large data sets
- Implement proper memory management
- Optimize network calls to minimize data usage