# Security Policy

<p align="center"><a href="SECURITY.md">English</a> · <a href="docs/SECURITY_es.md">Español</a></p>

## Supported versions

JavaCalcFX is a small desktop application on a `0.x` version line. Only the
**latest release** receives fixes; older versions are not maintained.

| Version | Supported |
|---|---|
| latest `0.x` release | ✅ |
| anything older | ❌ |

## Reporting a vulnerability

Please **do not open a public issue** for a security problem.

- Preferred: on the repository's **Security** tab, use **"Report a
  vulnerability"** (GitHub private advisories).
- Or email **guillermo_amado@hotmail.es**.

Include the version, your operating system and steps to reproduce. This is a
personal project maintained in spare time, so please allow a reasonable window
for a reply; you will get an acknowledgement and, once a fix is released, credit
in the release notes if you want it.

## Scope

The app runs fully offline, bundles its own Java runtime, and has no network,
authentication, or file-write features beyond `java.util.prefs`, so the
practical attack surface is small. Reports about the build/release pipeline
(GitHub Actions, the `.msi` installer) are also welcome.
