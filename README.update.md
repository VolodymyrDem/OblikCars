# Manual updates (Windows)

## How it works
- Users download the latest `program.exe` from GitHub Releases.
- They replace the old EXE manually.

## Release flow
- Tag a release: `vX.Y.Z`
- GitHub Actions builds `program.exe` and uploads it to the release assets.

## Notes
- Code signing is not used.
- Updates are manual by design.


