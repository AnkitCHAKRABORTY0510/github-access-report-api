# 🔐 GitHub Access Report API

> A Spring Boot service that connects to the GitHub REST API and generates a detailed report of **which users have access to which repositories** across any GitHub organization or user account — with support for parallel processing, pagination, and permission-level granularity.

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)
![GitHub API](https://img.shields.io/badge/GitHub_API-REST_v3-181717?style=flat-square&logo=github&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-UI-85EA2D?style=flat-square&logo=swagger&logoColor=black)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [Authentication](#-authentication)
- [API Reference](#-api-reference)
- [GitHub APIs Used](#-github-apis-used)
- [Design Decisions](#-design-decisions)
- [Assumptions](#-assumptions)
- [Future Improvements](#-future-improvements)
- [Author](#-author)

---

## 🧭 Overview

The **GitHub Access Report API** is a Spring Boot microservice that wraps the GitHub REST API v3 to produce a consolidated, auditable view of repository access across an organization or user account.

### Problem It Solves

GitHub's native UI does not provide a single view of "who can access what" at the organization level. Auditing access typically requires visiting each repository individually. This service automates that process by:

1. Fetching all repositories for a given GitHub `owner` (org or user)
2. For each repository, querying its collaborators (or contributors as fallback)
3. Aggregating the results into a unified `user → [repositories]` map
4. Returning the full report as a structured JSON response

### Use Cases

- **Security audits** — identify which users have `write` or `admin` access to sensitive repos
- **Offboarding checks** — verify a user has been removed from all repositories
- **Compliance reporting** — generate access snapshots for SOC 2 or ISO 27001 reviews
- **Organization hygiene** — detect contributors on archived or deprecated repositories

---

## ✨ Features

| Feature | Details |
|---|---|
| **GitHub REST API v3 integration** | Communicates with `api.github.com` using Bearer token auth |
| **Organization & user support** | Works for both GitHub orgs (`/orgs/{org}/repos`) and personal accounts (`/users/{username}/repos`) |
| **Permission-level reporting** | Reports `read`, `write`, `admin`, `maintain`, or `triage` permissions per user per repo |
| **Collaborator + contributor resolution** | Tries collaborator API first; falls back to contributor API if access is restricted |
| **Parallel repo processing** | Uses Java parallel streams to process multiple repos concurrently |
| **Paginated endpoint** | Supports `page` + `size` query params for large organizations |
| **Thread-safe aggregation** | Uses `ConcurrentHashMap` to safely collect results across threads |
| **Swagger UI** | Full interactive API docs at `/swagger-ui/index.html` |

---

## 🏗️ Architecture

### Layer Overview

The application is structured in three layers:

```
HTTP Request
     │
     ▼
┌─────────────────────┐
│     Controller      │  Handles HTTP routing, request validation, response shaping
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│      Service        │  Business logic: repo fetching, parallel processing, aggregation
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│    GitHub Client    │  Wraps GitHub REST API calls, manages auth headers + fallback logic
└────────┬────────────┘
         │
         ▼
   GitHub REST API
```

### Project Structure

```
src/main/java/com/example/githubaccessreport/
│
├── controller/
│   ├── GithubController.java        # Exposes /api/github/access-report endpoints
│   └── HealthController.java        # Exposes GET / health check
│
├── service/
│   └── GithubAccessService.java     # Core logic: repo iteration, parallel processing,
│                                    # result aggregation via ConcurrentHashMap
│
├── client/
│   └── GithubApiClient.java         # HTTP calls to api.github.com with Bearer token auth;
│                                    # handles collaborator → contributor fallback
│
├── config/
│   └── GithubConfig.java            # Reads GITHUB_TOKEN + API base URL from properties;
│                                    # configures shared RestTemplate/WebClient bean
│
└── model/
    ├── RepoAccess.java              # { repository: String, permission: String }
    └── UserAccess.java              # { username: String, repositories: List<RepoAccess> }
```

### Data Flow

```
owner (org/user)
      │
      ├─▶ GET /orgs/{owner}/repos  ──┐
      │                              ├─▶ List<Repository>
      └─▶ GET /users/{owner}/repos ──┘
                    │
          ┌─────────▼──────────┐
          │  Parallel Stream    │  (one thread per repo)
          └─────────┬──────────┘
                    │
          ┌─────────▼──────────────────────┐
          │  Per repo: GET /collaborators   │
          │  (fallback: GET /contributors)  │
          └─────────┬──────────────────────┘
                    │
          ┌─────────▼──────────────────────┐
          │  ConcurrentHashMap             │
          │  username → List<RepoAccess>   │
          └─────────┬──────────────────────┘
                    │
                    ▼
          List<UserAccess> (JSON response)
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| Java | 17 or higher |
| Maven | 3.8 or higher |
| GitHub Personal Access Token | Classic token with `repo` and `read:org` scopes |

#### Required Token Scopes

When generating your GitHub PAT at [github.com/settings/tokens](https://github.com/settings/tokens), enable:

- `repo` — Full repository access (required to list collaborators on private repos)
- `read:org` — Read-only access to organization membership and repositories

> ℹ️ For public organizations with only public repos, a token with no scopes may work but collaborator data will be limited.

---

### 1. Clone the Repository

```bash
git clone https://github.com/AnkitCHAKRABORTY0510/github-access-report-api.git
cd github-access-report-api
```

---

### 2. Create the Environment File

Create a `.env` file in the project root:

```env
GITHUB_TOKEN=your_github_personal_access_token
```

> ⚠️ **Security:** Add `.env` to your `.gitignore` immediately. Never commit tokens to version control.

```bash
echo ".env" >> .gitignore
```

---

### 3. Run the Application

The included `run.sh` script handles environment variable injection and application startup:

```bash
chmod +x run.sh
./run.sh
```

**What `run.sh` does internally:**

```bash
# Loads .env variables into the shell environment
export $(grep -v '^#' .env | xargs)

# Starts the Spring Boot application via Maven
./mvnw spring-boot:run
```

The application starts at:

```
http://localhost:8080
```

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

---

### 4. Verify the Service is Running

```bash
curl http://localhost:8080
# Response: GitHub Access Report Service Running
```

---

## 🔑 Authentication

All GitHub API requests are authenticated using a **Personal Access Token** passed as a Bearer token.

**`application.properties`:**

```properties
github.token=${GITHUB_TOKEN}
github.api.url=https://api.github.com
```

**Headers sent on every GitHub API request:**

```http
Authorization: Bearer <GITHUB_TOKEN>
Accept: application/vnd.github+json
X-GitHub-Api-Version: 2022-11-28
```

The token is injected into `GithubConfig.java` via `@Value("${github.token}")` and used to configure a shared HTTP client bean that all GitHub API calls flow through.

---

## 📖 API Reference

### `GET /`

**Health Check** — Verifies the service is running.

**Response `200 OK`:**

```
GitHub Access Report Service Running
```

---

### `GET /api/github/access-report/{owner}`

**Full Access Report** — Fetches all repositories for the given owner and returns a complete user-to-repository access map.

**Path Parameters:**

| Parameter | Type   | Required | Description |
|-----------|--------|----------|-------------|
| `owner`   | String | ✅ Yes   | GitHub organization name or username |

**Example Request:**

```bash
curl http://localhost:8080/api/github/access-report/google
```

**Response `200 OK`:**

```json
[
  {
    "username": "alice",
    "repositories": [
      {
        "repository": "repo1",
        "permission": "write"
      },
      {
        "repository": "repo2",
        "permission": "admin"
      }
    ]
  },
  {
    "username": "bob",
    "repositories": [
      {
        "repository": "repo1",
        "permission": "read"
      }
    ]
  }
]
```

**Permission values:** `read` · `triage` · `write` · `maintain` · `admin`

**Error Responses:**

| Status | Cause |
|--------|-------|
| `401 Unauthorized` | Invalid or missing GitHub token |
| `403 Forbidden` | Token lacks required scopes |
| `404 Not Found` | Owner does not exist on GitHub |

---

### `GET /api/github/access-report/{owner}/paged`

**Paginated Access Report** — Processes a subset of repositories per request. Recommended for organizations with 50+ repositories to avoid GitHub rate limiting and long response times.

**Parameters:**

| Parameter | Type    | Required | Default | Description |
|-----------|---------|----------|---------|-------------|
| `owner`   | String  | ✅ Yes   | —       | GitHub organization or username |
| `page`    | Integer | ✅ Yes   | `1`     | Page number (1-indexed) |
| `size`    | Integer | ✅ Yes   | `10`    | Number of repositories to process per page |

**Example Request:**

```bash
curl "http://localhost:8080/api/github/access-report/google/paged?page=1&size=10"
```

**Pagination Notes:**

- Page `1` processes repositories 1–10, page `2` processes 11–20, and so on.
- Repository ordering follows GitHub's default sort (by last push date).
- Each page is an independent request — results are not cumulative across pages.

---

## 🌐 GitHub APIs Used

| Purpose | Endpoint | Notes |
|----|----|----|
| List organization repositories | `GET /orgs/{org}/repos` | Used when owner is a GitHub organization |
| List user repositories | `GET /users/{username}/repos` | Used when owner is a personal account |
| List repository collaborators | `GET /repos/{owner}/{repo}/collaborators` | Returns users with explicit access + permission level |
| List repository contributors | `GET /repos/{owner}/{repo}/contributors` | Fallback; returns commit authors (no permission level) |

> ℹ️ The collaborator API requires the authenticated token to have admin access to the repository. If this returns `403`, the service automatically retries using the contributors endpoint.

---

## 🧠 Design Decisions

### ⚡ Parallel Processing with Java Streams

Repository processing uses `parallelStream()` to dispatch multiple GitHub API calls concurrently rather than sequentially:

```java
repos.parallelStream().forEach(repo -> {
    List<Collaborator> users = githubApiClient.getCollaborators(owner, repo);
    // aggregate into ConcurrentHashMap
});
```

For an organization with 100 repositories, this reduces total processing time from `100 × avg_latency` (sequential) to approximately `max_latency` across the thread pool — typically a 10–20× improvement depending on GitHub API response times.

### 🔄 Collaborators First, Contributors as Fallback

The collaborator API (`/repos/{owner}/{repo}/collaborators`) returns rich permission data but requires admin-level token access. When it returns `403`, the client retries with the contributors API (`/repos/{owner}/{repo}/contributors`), which is publicly accessible but only reflects commit authorship with no permission level.

**Tradeoff:** Broader coverage at the cost of permission granularity — contributor-based entries will have a `null` or default permission value.

### 📊 Aggregated User → Repository Mapping

Raw GitHub API responses are repo-centric: you query a repo and get back a list of users. The service inverts this into a user-centric view:

```
Before:  repo1 → [alice(write), bob(read)]
         repo2 → [alice(admin)]

After:   alice → [repo1(write), repo2(admin)]
         bob   → [repo1(read)]
```

This inversion makes the output directly useful for access audits and permission reviews without further client-side processing.

### 🔒 ConcurrentHashMap for Thread Safety

Because multiple threads write to the same aggregation map simultaneously via `parallelStream`, a standard `HashMap` would produce race conditions. `ConcurrentHashMap` with atomic `computeIfAbsent` ensures correctness without explicit locking:

```java
ConcurrentHashMap<String, List<RepoAccess>> userRepoMap = new ConcurrentHashMap<>();

userRepoMap.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>())
           .add(new RepoAccess(repoName, permission));
```

### 📄 Pagination Design

The paginated endpoint applies `page`/`size` windowing at the service layer after fetching the repository list from GitHub. This keeps the number of collaborator/contributor API calls proportional to the requested page size rather than the full organization size, avoiding unnecessary API usage and reducing response time.

---

## 📌 Assumptions

- The provided GitHub token has `repo` and `read:org` scopes for full collaborator data.
- Repositories returning `403` on the collaborator endpoint fall back to contributors silently — no error is surfaced to the caller.
- The service performs read-only operations and never writes to or modifies GitHub resources.
- GitHub API rate limits (5,000 requests/hour for authenticated tokens) are not currently enforced client-side; very large organizations may hit limits during full scans.

---

## 👤 Author

**Ankit Chakraborty**

[![GitHub](https://img.shields.io/badge/GitHub-AnkitCHAKRABORTY0510-181717?style=flat-square&logo=github)](https://github.com/AnkitCHAKRABORTY0510)

---

## 📂 Repository

```
https://github.com/AnkitCHAKRABORTY0510/github-access-report-api
```
