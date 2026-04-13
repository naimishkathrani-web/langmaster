Param(
  [string]$ProjectPath = "D:\Dev\langmaster"
)

$ErrorActionPreference = "Stop"
Set-Location "$ProjectPath\backend"
npm install
npm run build
npm run start
