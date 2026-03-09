# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a personal technical blog built with Jekyll using the [Chirpy theme](https://github.com/cotes2020/jekyll-theme-chirpy). The site contains posts about Java, Spring Cloud, Redis, Kafka, Elasticsearch, LLM, and other technical topics.

## Development Commands

### Local Development Server
```bash
./tools/run.sh
```
Runs Jekyll server with live reload at http://127.0.0.1:4000/tech-page/

### Build and Test
```bash
./tools/test.sh
```
Builds the site in production mode and runs html-proofer to validate links.

### Asset Building
```bash
npm run build        # Build JS & CSS
npm run build:css    # Build CSS only
npm run build:js     # Build JS only
npm run watch:js     # Watch JS files
npm run lint:scss    # Lint SCSS files
```

## Architecture

- **`_posts/`**: Blog posts in Markdown format with Jekyll front matter
- **`_tabs/`**: Static pages (about, archives, categories, tags)
- **`_data/`**: YAML data files for authors, locales, media links
- **`_includes/`**: Liquid template partials
- **`_layouts/`**: Jekyll layout templates
- **`_sass/`**: SCSS stylesheets
- **`assets/`**: Static assets (images, JS, CSS)
- **`_config.yml`**: Main Jekyll configuration
- **`tools/`**: Build and run scripts

## Commit Convention

This project uses conventional commits enforced by commitlint. Commit messages should follow the format:
```
<type>: <description>
```

Types include: `feat`, `fix`, `perf`, `refactor`, `docs`, `style`, `test`, `build`, `ci`, `chore`, `revert`
