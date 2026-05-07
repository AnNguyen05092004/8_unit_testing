param(
    [string]$OutputDir = "L:\LearnEz\TestTool\SystemAutomation\reports"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Host "Python is not installed or not available in PATH."
    exit 1
}

$env:LEARNEZ_AUTOMATION_OUTPUT = $OutputDir

python "L:\LearnEz\TestTool\SystemAutomation\learn_ez_system_automation.py"
