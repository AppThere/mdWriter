# Markdown Editor Documentation Package

Complete documentation for building a cross-platform Markdown editor with Kotlin Multiplatform and Compose.

## 📚 Documentation Files

### Core Documents

1. **[CLAUDE.md](CLAUDE.md)** - Master Project Guide
   - Complete project overview and architecture
   - Technical requirements for all features
   - 10-phase development roadmap
   - E Ink optimized design guidelines
   - 📖 ~450 lines | ⏱️ 15-20 min read

2. **[DOCUMENT_FORMAT_SPEC.md](DOCUMENT_FORMAT_SPEC.md)** - Data Format Specification
   - Complete JSON schema for document storage
   - **Stylesheet linking and application rules** ⭐
   - Validation rules and error handling
   - Multiple complete examples
   - 📖 ~500 lines | ⏱️ 15 min read

3. **[SYNC_ARCHITECTURE_SPEC.md](SYNC_ARCHITECTURE_SPEC.md)** - Cloud Sync Design
   - Provider-agnostic sync architecture
   - Implementation guides (Google Drive, OneDrive, Dropbox)
   - Conflict detection and resolution
   - **Enhanced with resource sync support** ⭐
   - 📖 ~600 lines | ⏱️ 20 min read

### New Feature Specifications ⭐

4. **[EPUB_EXPORT_SPEC.md](EPUB_EXPORT_SPEC.md)** - EPUB 3.3 Export ⭐ NEW
   - Complete EPUB 3.3 export specification
   - ZIP archive structure and requirements
   - Markdown to XHTML conversion rules
   - Font embedding and CSS generation
   - Package document, navigation, and manifest
   - 📖 ~550 lines | ⏱️ 20 min read

5. **[FILE_ASSOCIATION_SPEC.md](FILE_ASSOCIATION_SPEC.md)** - File Association & Landing Page ⭐ NEW
   - Landing page design and implementation
   - Recent documents tracking
   - .mdoc file extension and MIME type
   - Platform-specific file association (Android, iOS, Desktop)
   - Deep linking and intent handling
   - 📖 ~450 lines | ⏱️ 15 min read

6. **[CSS_EDITOR_AND_SYNC_SPEC.md](CSS_EDITOR_AND_SYNC_SPEC.md)** - Enhanced Features ⭐ NEW
   - CSS autocomplete with intelligent suggestions
   - Font-family autocomplete with embedded fonts
   - Font management with licensing warnings
   - Cloud sync with external resources (fonts, images)
   - Resource tracking and upload
   - 📖 ~400 lines | ⏱️ 15 min read

### Implementation Guides

7. **[IMPLEMENTATION_PROMPTS.md](IMPLEMENTATION_PROMPTS.md)** - Core Feature Prompts
   - 16 ready-to-use prompts for core features
   - Budget management strategy
   - Session planning guide
   - 📖 ~400 lines | ⏱️ Copy & paste as needed

8. **[ADDITIONAL_PROMPTS.md](ADDITIONAL_PROMPTS.md)** - New Feature Prompts ⭐ NEW
   - 8 additional prompts for new features
   - EPUB export (3 phases)
   - Landing page & file association
   - CSS autocomplete & font management
   - Enhanced resource sync
   - 📖 ~350 lines | ⏱️ Copy & paste as needed

### Reference Materials

9. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Code Patterns & Snippets
   - Copy-paste ready code examples
   - **Stylesheet resolution implementation** ⭐
   - E Ink color palettes
   - Common patterns and utilities
   - 📖 ~500 lines | ⏱️ Quick reference

10. **[STYLESHEET_ARCHITECTURE.md](STYLESHEET_ARCHITECTURE.md)** - Visual Stylesheet Guide
    - **Detailed visual diagrams of stylesheet linking** ⭐
    - Complete data flow examples
    - Common patterns and use cases
    - Implementation examples
    - 📖 ~300 lines | ⏱️ 10 min read

11. **[USAGE_GUIDE.md](USAGE_GUIDE.md)** - How to Use These Files
    - Where to place files in your project
    - How to reference docs in Claude Code prompts
    - **Practical stylesheet linking examples** ⭐
    - Workflow recommendations
    - 📖 ~400 lines | ⏱️ 10 min read

12. **[README_DOCS.md](README_DOCS.md)** - This File
    - Overview of all documentation
    - Quick answers to common questions
    - Getting started guide

## ⭐ Answer to Your Questions

### Q1: Does this handle linking Markdown blocks to CSS stylesheets?

**Yes!** The specification now includes comprehensive stylesheet linking:

- **Section-level linking**: Each `Section` has a `stylesheets` array that references stylesheet IDs
- **Global stylesheets**: Apply to all sections via `scope: "global"` or `settings.defaultStylesheets`
- **CSS class annotations**: `{.class-name}` in Markdown references styles from active stylesheets
- **Priority system**: Control which styles take precedence

**See**:
- DOCUMENT_FORMAT_SPEC.md → "Stylesheet Linking & Application" section
- STYLESHEET_ARCHITECTURE.md → Complete visual guide
- QUICK_REFERENCE.md → `StylesheetResolver` implementation

### Q2: How do I use these files in my project?

**Quick Start**:

1. **Place files at project root**:
   ```
   your-project/
   ├── CLAUDE.md                     ← Place here
   ├── DOCUMENT_FORMAT_SPEC.md       ← Place here
   ├── SYNC_ARCHITECTURE_SPEC.md     ← Place here
   ├── IMPLEMENTATION_PROMPTS.md     ← Place here
   ├── QUICK_REFERENCE.md            ← Place here
   ├── USAGE_GUIDE.md                ← Place here
   ├── STYLESHEET_ARCHITECTURE.md    ← Place here
   ├── build.gradle.kts
   └── composeApp/
   ```

2. **Reference in Claude Code prompts**:
   ```
   Please read CLAUDE.md and DOCUMENT_FORMAT_SPEC.md, then implement 
   the Document data model with stylesheet linking as specified.
   ```

3. **Use prepared prompts**:
   - Open IMPLEMENTATION_PROMPTS.md
   - Copy Prompt 1 (Project Setup)
   - Paste into Claude Code
   - Continue sequentially through prompts

**See**: USAGE_GUIDE.md for complete instructions and examples

## 🚀 Getting Started

### Step 1: Read Core Documents (45 min)
1. **CLAUDE.md** - Overview and architecture
2. **DOCUMENT_FORMAT_SPEC.md** - Focus on stylesheet sections
3. **STYLESHEET_ARCHITECTURE.md** - Visual understanding

### Step 2: Start Implementation (First Session)
1. Copy Prompt 1 from IMPLEMENTATION_PROMPTS.md
2. Paste into Claude Code
3. Let Claude Code set up your project structure

### Step 3: Continue Development
- Follow prompts 2-16 sequentially
- Reference QUICK_REFERENCE.md for code patterns
- Use USAGE_GUIDE.md when you need help

## 📊 Stylesheet Linking Quick Reference

### Data Structure
```json
{
  "stylesheets": [
    {"id": "base", "scope": "global", "content": "body{...}"},
    {"id": "fancy", "scope": "manual", "content": ".special{...}"}
  ],
  "sections": {
    "intro": {
      "stylesheets": [],           // Uses only global
      "content": "# Introduction"
    },
    "chapter": {
      "stylesheets": ["fancy"],    // Uses global + fancy
      "content": "# Title {.special}"
    }
  },
  "settings": {
    "defaultStylesheets": ["base"]
  }
}
```

### Resolution Logic
1. Start with global stylesheets (`scope: "global"`)
2. Add default stylesheets (`settings.defaultStylesheets`)
3. Add section-specific stylesheets (`section.stylesheets`)
4. Sort by priority (lower = first)
5. Merge CSS content

### Implementation
```kotlin
// See QUICK_REFERENCE.md for complete code
class StylesheetResolver(document: Document) {
    fun getStylesheetsForSection(sectionId: String): List<Stylesheet>
    fun getCombinedCss(sectionId: String): String
    fun isClassDefined(sectionId: String, className: String): Boolean
}
```

## 💰 Budget Strategy ($250 Credits)

### High Priority (Complete First): ~6-8 hours
- ✅ Prompts 1-5: Core functionality
- ✅ Prompts 6-8: Basic editor
- ✅ Prompt 11: Sync foundation

### Medium Priority: ~3-4 hours
- Prompts 9-10: Document management
- Prompts 12-13: Cloud sync providers

### Lower Priority: ~2-3 hours
- Prompt 14: Accessibility
- Prompt 15-16: Testing & polish

**Tip**: Start each session with a prepared prompt from IMPLEMENTATION_PROMPTS.md to minimize discussion time and maximize implementation.

## 🎯 Key Features Covered

### Core Features
- ✅ Multipart Markdown documents in JSON format
- ✅ Hugo-style frontmatter support
- ✅ **CSS stylesheet system with flexible linking** ⭐
- ✅ CSS class annotations `{.class-name}`
- ✅ Real-time syntax highlighting
- ✅ E Ink optimized color scheme
- ✅ Responsive design (phone, tablet, desktop)
- ✅ Cloud sync (Google Drive, OneDrive, Dropbox)
- ✅ Conflict resolution
- ✅ Accessibility (WCAG AA)
- ✅ Comprehensive testing

### New Features ⭐
- ✅ **EPUB 3.3 Export** - Convert documents to valid EPUB publications
- ✅ **Landing Page** - Recent documents with quick access
- ✅ **File Association** - .mdoc files open directly in app
- ✅ **CSS Autocomplete** - Intelligent property/value suggestions
- ✅ **Font Management** - Embed custom fonts with licensing warnings
- ✅ **Enhanced Cloud Sync** - Automatic upload of fonts and images
- ✅ **Font-Family Autocomplete** - Suggest embedded and system fonts

## 📖 Reading Order Recommendations

### For Quick Start:
1. USAGE_GUIDE.md (understand how to use docs)
2. IMPLEMENTATION_PROMPTS.md (start coding)
3. Reference others as needed

### For Deep Understanding:
1. CLAUDE.md (full overview)
2. DOCUMENT_FORMAT_SPEC.md (data format)
3. STYLESHEET_ARCHITECTURE.md (visual guide)
4. SYNC_ARCHITECTURE_SPEC.md (sync details)
5. QUICK_REFERENCE.md (bookmark for later)

### For Stylesheet Understanding:
1. STYLESHEET_ARCHITECTURE.md (visual diagrams) ⭐
2. DOCUMENT_FORMAT_SPEC.md → "Stylesheet Linking" section
3. QUICK_REFERENCE.md → "Stylesheet Resolution Pattern"
4. USAGE_GUIDE.md → Stylesheet examples

## 🛠️ Development Tools

### Required Dependencies
- Kotlin Multiplatform 1.9+
- Compose Multiplatform 1.6+
- kotlinx.serialization
- kotlinx.coroutines
- kotlinx.datetime

### Recommended Tools
- Android Studio / IntelliJ IDEA
- Claude Code for implementation
- Git for version control

### Testing Tools
- JUnit 5
- Kotest
- MockK
- Compose UI Test

## 📝 Updating Documentation

As you build, keep docs updated:

```bash
# Commit docs to version control
git add *.md
git commit -m "Add/update project documentation"

# Update as you learn
- Add patterns to QUICK_REFERENCE.md
- Document decisions in CLAUDE.md
- Update timelines in IMPLEMENTATION_PROMPTS.md
```

## 🤝 Contributing to Your Own Docs

**When you discover**:
- Better patterns → Add to QUICK_REFERENCE.md
- Issues/bugs → Document solutions in CLAUDE.md
- New features → Update DOCUMENT_FORMAT_SPEC.md
- Time estimates → Adjust IMPLEMENTATION_PROMPTS.md

## 📞 Getting Help

**If stuck on implementation**:
```
Claude Code, I'm having trouble with [issue]. Please review the 
relevant sections in [DOC_NAME.md] and suggest a solution.
```

**If specs are unclear**:
```
Claude Code, can you explain the stylesheet linking system described 
in DOCUMENT_FORMAT_SPEC.md and STYLESHEET_ARCHITECTURE.md with a 
concrete example?
```

## ✅ Verification Checklist

Before starting development, ensure:

- [ ] All documentation files at project root
- [ ] Read CLAUDE.md (overview)
- [ ] Read USAGE_GUIDE.md (how to use docs)
- [ ] Read STYLESHEET_ARCHITECTURE.md (stylesheet system)
- [ ] Reviewed IMPLEMENTATION_PROMPTS.md
- [ ] Have Claude Code access
- [ ] Project structure created
- [ ] Git repository initialized

## 🎓 Learning Resources

**Understanding the Architecture**:
- CLAUDE.md → "Core Architecture"
- STYLESHEET_ARCHITECTURE.md → Visual diagrams

**Implementation Patterns**:
- QUICK_REFERENCE.md → All patterns
- USAGE_GUIDE.md → Practical examples

**Specifications**:
- DOCUMENT_FORMAT_SPEC.md → Complete format
- SYNC_ARCHITECTURE_SPEC.md → Sync details

## 🏆 Success Criteria

Your project is successful when:

### Core Features
1. ✅ Documents load/save in JSON format
2. ✅ Markdown parsing works with frontmatter
3. ✅ **Stylesheets link correctly to sections** ⭐
4. ✅ **CSS classes work with {.class} syntax** ⭐
5. ✅ Syntax highlighting works in real-time
6. ✅ UI is responsive across devices
7. ✅ E Ink theme looks good
8. ✅ Cloud sync works with conflict resolution
9. ✅ Tests pass with >80% coverage
10. ✅ Accessible to screen readers

### New Features ⭐
11. ✅ **App opens with landing page showing recent documents**
12. ✅ **Double-clicking .mdoc files opens app with file loaded**
13. ✅ **CSS editor provides intelligent autocomplete**
14. ✅ **Fonts can be embedded with proper licensing warnings**
15. ✅ **Documents export to valid EPUB 3.3 format**
16. ✅ **EPUB passes EPUBCheck validation**
17. ✅ **EPUB opens correctly in major readers (Calibre, Apple Books)**
18. ✅ **Fonts and images sync automatically to cloud storage**

## 📈 Next Steps

1. **Now**: Place these files in your project root
2. **Read** (30 min):
   - README_DOCS.md (this file) - Overview
   - CLAUDE.md - Core architecture
   - EPUB_EXPORT_SPEC.md - EPUB export details
   - FILE_ASSOCIATION_SPEC.md - Landing page & file association
3. **Start**: Use Prompt 1 from IMPLEMENTATION_PROMPTS.md in Claude Code
4. **Build Core Features**: Work through Prompts 2-10 sequentially
5. **Add New Features**: Use ADDITIONAL_PROMPTS.md (Prompts 11A-18)
6. **Test & Polish**: Complete integration and testing

**Package**: `com.appthere.mdwriter`  
**File Extension**: `.mdoc`  
**MIME Type**: `application/vnd.appthere.mdwriter+json`

---

**Documentation Version**: 1.0  
**Last Updated**: 2025-11-08  
**Maintained By**: Your Project Team

**Questions?** Review USAGE_GUIDE.md or ask Claude Code for clarification on any topic.
