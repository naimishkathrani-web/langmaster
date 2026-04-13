Param(
  [string]$RepoUrl = "https://github.com/naimishkathrani-web/langmaster.git",
  [string]$Workspace = "D:\Dev"
)

$ErrorActionPreference = "Stop"

if (!(Test-Path $Workspace)) {
  New-Item -ItemType Directory -Path $Workspace | Out-Null
}

Set-Location $Workspace
if (!(Test-Path "$Workspace\langmaster")) {
  git clone $RepoUrl
}

Set-Location "$Workspace\langmaster"
Write-Host "Repository ready at $Workspace\langmaster"
Write-Host "Next: open Android Studio and run :app:assembleDebug"
