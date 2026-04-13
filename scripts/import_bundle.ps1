Param(
  [string]$RepoPath = "D:\Dev\langmaster",
  [string]$BundlePath = "D:\Dev\langmaster-work.bundle",
  [string]$Branch = "work"
)

$ErrorActionPreference = "Stop"

Set-Location $RepoPath
git fetch $BundlePath $Branch
git checkout $Branch
git pull --ff-only $BundlePath $Branch

Write-Host "Imported bundle '$BundlePath' into '$RepoPath' on branch '$Branch'."
