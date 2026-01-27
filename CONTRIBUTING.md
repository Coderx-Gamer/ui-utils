# Contributing to UI-Utils Advanced

Thank you for your interest in contributing to UI-Utils Advanced! This is a **fork** of the original [UI-Utils](https://github.com/Coderx-Gamer/ui-utils) by Coderx-Gamer, maintained by FrannnDev with additional features and improvements.

---

## About This Fork

This project is based on the original UI-Utils mod. We maintain compatibility with the original while adding:
- Multi-version support (1.21 - 1.21.11)
- ViaFabricPlus integration
- Additional packet manipulation features
- Improved UI and user experience

---

## Code of Conduct

By participating in this project, you agree to:

- Be respectful and inclusive to all contributors
- Provide constructive feedback
- Accept constructive criticism gracefully
- Focus on what is best for the community and project
- Report any unacceptable behavior to the maintainers

---

## How to Contribute

### Reporting Bugs

Before submitting a bug report:

1. Check if the issue already exists in [GitHub Issues](https://github.com/FrannnnDev/ui-utils-advanced/issues)
2. Make sure you're using the latest version
3. Collect relevant information:
   - Minecraft version
   - Fabric Loader version
   - Fabric API version
   - Other mods installed (if any)
   - Crash log or error message

When reporting:

- Use a clear and descriptive title
- Describe the steps to reproduce the issue
- Explain the expected vs actual behavior
- Include screenshots or videos if applicable

### Suggesting Features

Feature requests are welcome! Please:

1. Check if the feature was already requested
2. Provide a clear description of the feature
3. Explain the use case and why it would be useful
4. Consider how it fits with the project's scope

### Pull Requests

#### Before Submitting

1. Fork the repository
2. Create a new branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Make your changes following our coding standards
4. Test your changes on all supported Minecraft versions if possible
5. Commit with clear, descriptive messages

#### Pull Request Guidelines

- Keep PRs focused on a single feature or fix
- Update documentation if needed
- Add comments for complex logic
- Follow existing code style and patterns
- Ensure the build passes before submitting

---

## Coding Standards

### Java Style Guide

- Use 4 spaces for indentation (no tabs)
- Opening braces on the same line
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods short and focused (single responsibility)

### Example

```java
/**
 * Creates a packet compatible with all Minecraft versions.
 * @param onGround Whether the player is on the ground
 * @return The constructed packet or null if creation fails
 */
private static PlayerMoveC2SPacket createOnGroundPacket(boolean onGround) {
    try {
        // Implementation here
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
```

### Mixin Guidelines

- Use descriptive mixin class names (e.g., `ChatScreenMixin`)
- Document what each mixin modifies
- Use `@Inject` with clear target methods
- Avoid modifying more than necessary
- Test mixins across all supported versions

---

## Multi-Version Compatibility

UI-Utils supports Minecraft 1.21 through 1.21.11. When contributing:

1. **Use reflection** for API differences between versions
2. **Test on multiple versions** when possible
3. **Document version-specific behavior** in comments
4. **Never use version-specific code directly** - always use compatibility wrappers

### Example: Version-Compatible Code

```java
// BAD - Will fail on older versions
new PlayerMoveC2SPacket.OnGroundOnly(true, false);

// GOOD - Uses reflection for compatibility
private static PlayerMoveC2SPacket createOnGroundPacket(boolean onGround, boolean collision) {
    try {
        Constructor<?> constructor = PlayerMoveC2SPacket.OnGroundOnly.class
            .getConstructor(boolean.class, boolean.class);
        return (PlayerMoveC2SPacket) constructor.newInstance(onGround, collision);
    } catch (NoSuchMethodException e) {
        // Fallback for older versions
        Constructor<?> constructor = PlayerMoveC2SPacket.OnGroundOnly.class
            .getConstructor(boolean.class);
        return (PlayerMoveC2SPacket) constructor.newInstance(onGround);
    }
}
```

---

## Building the Project

### Single Version Build

```bash
./gradlew build
```

### Multi-Version Build

```bash
./build-all.bat
```

Output JARs will be in `build/multiversion/`

---

## Commit Message Format

Use clear, descriptive commit messages:

```
<type>: <short description>

[optional body with more details]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `style` | Code style changes (formatting, etc.) |
| `refactor` | Code refactoring |
| `test` | Adding or updating tests |
| `chore` | Maintenance tasks |

### Examples

```
feat: Add hClip command for horizontal teleportation

fix: Resolve crash when opening inventory on 1.21.3

docs: Update README with new button descriptions

refactor: Use reflection for multi-version packet compatibility
```

---

## Review Process

1. All PRs require at least one maintainer review
2. CI must pass (if configured)
3. Code must follow project standards
4. Changes must be tested on relevant versions
5. Documentation must be updated if needed

---

## License

By contributing to UI-Utils Advanced, you agree that your contributions will be licensed under the same license as the project (see [LICENSE](LICENSE)).

**Note:** This fork respects the original license from Coderx-Gamer's UI-Utils. Please ensure your contributions are compatible with both projects.

---

## Credits

- **Original Author:** [Coderx-Gamer](https://github.com/Coderx-Gamer/ui-utils)
- **Fork Maintainer:** [FrannnDev](https://github.com/FrannnnDev)

---

## Questions?

If you have questions about contributing:

1. Check existing issues and discussions
2. Open a new issue with the `question` label
3. Contact maintainers through GitHub

---

Thank you for contributing to UI-Utils Advanced!


