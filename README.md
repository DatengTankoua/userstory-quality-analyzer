# User Story Analyzer

> An NLP-powered desktop application for automated quality analysis of Agile user stories — developed at Philipps-Universität Marburg (SP25 · Group 1).

[![Pipeline](https://gitlab.uni-marburg.de/stechert/sp25_gruppe1_dateng_hammudi_stechert_alshaabi/badges/main/pipeline.svg)](https://gitlab.uni-marburg.de/stechert/sp25_gruppe1_dateng_hammudi_stechert_alshaabi/-/pipelines)
[![Coverage](https://gitlab.uni-marburg.de/stechert/sp25_gruppe1_dateng_hammudi_stechert_alshaabi/badges/main/coverage.svg)](https://gitlab.uni-marburg.de/stechert/sp25_gruppe1_dateng_hammudi_stechert_alshaabi/-/jobs)
[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange?logo=openjdk)](https://adoptium.net/)
[![JavaFX 21](https://img.shields.io/badge/JavaFX-21-blue?logo=java)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-red?logo=apachemaven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Academic-lightgrey)](LICENSE)

---

## Overview

**User Story Analyzer** automatically evaluates the quality of Agile user stories against **8 linguistic and structural criteria** using state-of-the-art NLP libraries (Apache OpenNLP · Stanford CoreNLP · WordNet). It provides a rich JavaFX desktop GUI, structured JSON exports, and batch analysis capabilities — helping software engineering teams maintain high-quality product backlogs.

---

## Features

| Feature | Description |
|---|---|
| **8 Quality Criteria** | Well-formedness, Atomicity, Uniformity, Minimality, Completeness, Non-redundancy, Independence, Non-conflict |
| **NLP Pipeline** | POS tagging, dependency parsing, lemmatisation via Apache OpenNLP & Stanford CoreNLP |
| **Semantic Analysis** | WordNet-based synonym/hyponym detection (JWNL) for redundancy and conflict checks |
| **JavaFX GUI** | Interactive desktop interface — load files, select criteria, inspect results per story |
| **JSON Export** | Machine-readable quality reports and structured user story models |
| **Ground Truth** | Bundled annotated datasets for benchmark evaluation |
| **Unit Tests** | JUnit 5 test suite with JaCoCo code-coverage reporting |
| **CI/CD Pipeline** | Fully automated build → test → package → release pipeline on GitLab CI |

---

## Quality Criteria

```
As a <role>, I want <feature> [so that <benefit>].
    │             │                    │
    │             └─ Atomicity         └─ Minimality
    └─ Completeness
         │
         ├─ Well-formedness   (correct syntactic structure)
         ├─ Uniformity        (consistent phrasing across all stories)
         ├─ Non-redundancy    (no duplicate semantic content)
         ├─ Independence      (no cross-story coupling)
         └─ Non-conflict      (no contradictory intents)
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 LTS |
| GUI | JavaFX 21 |
| NLP | Apache OpenNLP 2.3, Stanford CoreNLP 4.5, JWNL 1.4 |
| ML Backend | DeepLearning4J 1.0.0-M2.1, ND4J Native |
| Serialisation | Jackson 2.15 |
| Testing | JUnit Jupiter 5.9, JaCoCo 0.8 |
| Build | Maven 3.9+ |
| CI/CD | GitLab CI/CD |

---

## Project Structure

```
src/
├── main/java/de/uni_marburg/userstoryanalyzer/
│   ├── analysis/          # Quality criteria implementations
│   │   ├── QualityAnalyzer.java
│   │   ├── Wohlgeformtheit.java    (Well-formedness)
│   │   ├── Atomaritaet.java        (Atomicity)
│   │   ├── Uniformitaet.java       (Uniformity)
│   │   ├── Minimalitaet.java       (Minimality)
│   │   ├── Vollstaendigkeit.java   (Completeness)
│   │   ├── Redundanzfreiheit.java  (Non-redundancy)
│   │   ├── Unabhaengigkeit.java    (Independence)
│   │   └── Konfliktfreiheit.java   (Non-conflict)
│   ├── gui/               # JavaFX application
│   ├── model/             # Domain model (UserStory, Action, Entity …)
│   ├── parser/            # OpenNLP-based story parser
│   └── export/            # JSON export
├── main/resources/
│   ├── models/            # NLP model files
│   └── json-files/        # Sample input & output
└── test/java/             # JUnit 5 test classes
```

---

## Prerequisites

| Requirement | Minimum Version |
|---|---|
| JDK | **21 LTS** (Eclipse Temurin or Oracle) |
| Maven | **3.9+** |
| OS | Windows 10 / macOS 12 / Ubuntu 22.04 |
| RAM | 4 GB (8 GB recommended — Stanford CoreNLP is memory-intensive) |

---

## Installation & Quick Start

### 1 — Install JDK 21

**Windows (winget)**
```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

**macOS (Homebrew)**
```bash
brew install --cask temurin@21
```

**Ubuntu / Debian**
```bash
sudo apt update && sudo apt install temurin-21-jdk
```

Verify:
```bash
java -version   # should print: openjdk version "21 ..."
```

### 2 — Set JAVA_HOME to JDK 21

> **Important:** If you have multiple JDKs installed, Maven must use JDK 21 — not an older version. The error `invalid target release: 21` means `JAVA_HOME` points to an older JDK.

**Windows — current session only**
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
java -version   # confirm: 21
```

**Windows — permanent (System Properties → Environment Variables)**
1. Open *System Properties* → *Environment Variables*
2. Set `JAVA_HOME` = `C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot`
3. Move `%JAVA_HOME%\bin` to the top of the `Path` variable
4. Restart your terminal

**macOS / Linux**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
# or
export JAVA_HOME=/usr/lib/jvm/temurin-21            # Linux
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

### 3 — Clone the Repository

```bash
git clone https://gitlab.uni-marburg.de/stechert/sp25_gruppe1_dateng_hammudi_stechert_alshaabi.git
cd sp25_gruppe1_dateng_hammudi_stechert_alshaabi
```

### 4 — Build & Run Tests

```bash
# Compile + run all unit tests + generate coverage report
mvn clean verify

# Coverage report is written to:
#   target/site/jacoco/index.html
```

### 5 — Run the Application

#### Option A — JavaFX GUI (recommended)
```bash
mvn javafx:run
```

#### Option B — Headless CLI
```bash
mvn clean package -DskipTests
java -jar target/userstory-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The CLI parser reads user stories from `src/main/resources/models/g03-loudoun.txt`  
and writes the structured result to `src/main/resources/json-files/userstories.json`.

---

## Using the GUI

1. **Launch** — `mvn javafx:run`
2. **Load** — Click *Open File* and choose a `.txt` file with one user story per line
3. **Select criteria** — Tick the quality dimensions you want to evaluate
4. **Analyse** — Click *Analyse*; results appear in tabbed panels per criterion
5. **Export** — Click *Export JSON* to save the full quality report

Expected user story format:
```
As a project manager, I want to track sprint velocity so that I can forecast future capacity.
As a developer, I want automated test execution so that regressions are caught immediately.
```

---

## CI/CD Pipeline

The project ships with a fully automated **4-stage GitLab CI/CD pipeline**:

```
feature/*  ──►  [build] ──► [test]
develop    ──►  [build] ──► [test] ──► [package] ──► [deploy:staging]   (automatic)
main       ──►  [build] ──► [test] ──► [package] ──► [deploy:production] (manual gate)
```

| Stage | Job | Trigger | Artefact |
|---|---|---|---|
| **build** | `compile` | every push | compiled classes |
| **test** | `unit-tests` | every push | JUnit XML + JaCoCo HTML report |
| **package** | `package:jar` | develop / main / release/* | fat JAR |
| **deploy** | `deploy:staging` | develop (automatic) | staging environment link |
| **deploy** | `deploy:production` | main (manual approval) | GitLab Release + JAR asset |

The pipeline uses **Maven dependency caching** keyed on `pom.xml` to keep build times fast.

---

## Running Tests Only

```bash
mvn clean test
```

Test reports: `target/surefire-reports/`  
Coverage report: `target/site/jacoco/index.html`

---

## Authors

| Name | Role |
|---|---|
| Ayham Alshaabi | GUI Development, Quality Criteria |
| Hammudi | NLP Pipeline, Parser |
| Stechert | Architecture, CI/CD |
| Dateng | Analysis Engine, Testing |

Philipps-Universität Marburg — Software Project 2025

---

## License

This project was created for academic purposes at Philipps-Universität Marburg.
