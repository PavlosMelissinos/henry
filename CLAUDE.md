# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

henry is a Clojure library for generating task dependency graphs and Gantt charts from declarative specifications. It provides both programmatic API and CLI interface for converting task definitions into various visualization formats.

## Core Architecture

### Main Components

- **henry.core** - Primary API with `convert` and `export` functions for transforming specs into visualizations
- **henry.cli** - Command-line interface with modes for deps (:deps) and gantt (:gantt) chart generation
- **henry.gantt** - Gantt chart generation using Vega-Lite specs, exports to SVG/JSON/HTML via darkstar
- **henry.tangle** - Task dependency graph generation using Graphviz dot format, exports to PNG
- **henry.graph** - Core graph manipulation and task scheduling algorithms
- **henry.config** - Clojure spec definitions for input validation
- **henry.utils** - Shared utilities for file I/O and styling

### Data Flow

1. Input: EDN spec with `:tasks`, `:dependencies`, and `:styles`
2. Graph processing: Build dependency graph and assign task start times
3. Visualization: Convert to format-specific representation (dot/vega-lite)
4. Export: Generate final output (PNG, SVG, JSON, HTML)

## Development Commands

### Running Tests
```bash
# Run all tests via Kaocha
clj -M:test

# Run specific test namespace
clj -M:test --focus henry.graph-test
```

### CLI Usage
```bash
# Generate dependency graph (PNG)
clj -M:task-dep test_resources/data.edn

# Generate Gantt chart (SVG) 
clj -M:gantt test_resources/data.edn

# Custom output format and file
clj -m henry.cli --mode gantt --format svg --output-file output.svg test_resources/data.edn
```

### Build
```bash
# Compile to native image (requires GraalVM)
clj -M:native-image

# Generated binary will be named 'henry'
```

### REPL Development
```bash
# Start REPL with dev profile
clj -M:dev

# In REPL, common development workflows:
(require '[henry.core :as henry])
(def spec (henry/load-edn "test_resources/data.edn"))
(henry/convert spec :gantt :svg)
(henry/export spec :deps :png "output.png")
```

## Key Dependencies

- **weavejester/dependency** - Core graph manipulation
- **macroz/tangle** - Graphviz integration for dependency graphs  
- **applied-science/darkstar** - Vega-Lite to SVG conversion
- **metasoarous/oz** - Vega-Lite visualization (HTML output)
- **lambdaisland/kaocha** - Test runner with profiling

## Input Specification Format

Tasks are defined as EDN maps with required `:id` and `:duration`, optional `:styles`:

```clojure
{:description "Project roadmap"
 :tasks [{:id :task-a :duration 3 :styles [:critical]}
         {:id :task-b :duration 2 :styles [:major]}]
 :dependencies [[:task-b :task-a]]  ; [dependent prerequisite] 
 :styles {:critical {:fillcolor "#FF0000" :style "filled"}}}
```

## Export Modes and Formats

- **:deps mode** - Task dependency graphs (PNG, DOT)
- **:gantt mode** - Gantt charts (SVG, JSON, HTML)

## Native Image Configuration

The project supports GraalVM native-image compilation via the `:native-image` alias with build-time initialization and direct linking optimizations.