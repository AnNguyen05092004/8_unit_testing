param(
    [Parameter(Mandatory = $false)]
    [ValidateSet("Load", "Stress", "All")]
    [string]$TestType = "All",

    [Parameter(Mandatory = $false)]
    [switch]$GenerateReport = $true,

    [Parameter(Mandatory = $false)]
    [switch]$OpenReport = $false
)

$ErrorActionPreference = "Stop"

$WorkspaceRoot = "L:\LearnEz"
$TestPlan = Join-Path $WorkspaceRoot "LearnEz_JMeter_TestPlan.jmx"
$ResultsDir = Join-Path $WorkspaceRoot "jmeter_results"
$ReportsDir = Join-Path $WorkspaceRoot "jmeter_reports"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$LogFile = Join-Path $ResultsDir ("jmeter_" + $Timestamp + ".log")
$JtlFile = Join-Path $ResultsDir ("results_" + $Timestamp + ".jtl")
$ReportDir = Join-Path $ReportsDir ("report_" + $Timestamp)

function Write-Section {
    param([string]$Message)
    Write-Host "`n==================================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "==================================================" -ForegroundColor Cyan
}

function Ensure-Directory {
    param([string]$Path)
    if (-not (Test-Path $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
    }
}

function Resolve-JMeterBat {
    $candidates = @()

    $candidates += "C:\Users\Admin\Downloads\apache-jmeter-5.6.3\apache-jmeter-5.6.3\bin\jmeter.bat"

    if ($env:JMETER_HOME) {
        $candidates += (Join-Path $env:JMETER_HOME "bin\jmeter.bat")
    }

    $candidates += "C:\apache-jmeter-5.6.3\bin\jmeter.bat"
    $candidates += "C:\apache-jmeter-5.6.2\bin\jmeter.bat"

    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    $jmeterCmd = Get-Command jmeter -ErrorAction SilentlyContinue
    if ($jmeterCmd) {
        return $jmeterCmd.Source
    }

    throw "JMeter not found. Set JMETER_HOME or install JMeter at C:\\apache-jmeter-5.6.3"
}

function Parse-JtlSummary {
    param([string]$Path)

    $rows = Import-Csv -Path $Path
    if (-not $rows -or $rows.Count -eq 0) {
        throw "JTL file is empty: $Path"
    }

    $total = $rows.Count
    $ok = ($rows | Where-Object { $_.success -eq "true" }).Count
    $failed = $total - $ok

    $elapsedValues = @($rows | ForEach-Object { [double]$_.elapsed } | Sort-Object)
    $avg = [math]::Round(($elapsedValues | Measure-Object -Average).Average, 2)
    $min = [math]::Round(($elapsedValues | Measure-Object -Minimum).Minimum, 2)
    $max = [math]::Round(($elapsedValues | Measure-Object -Maximum).Maximum, 2)

    $p95Index = [math]::Min([math]::Max([math]::Ceiling($elapsedValues.Count * 0.95) - 1, 0), $elapsedValues.Count - 1)
    $p99Index = [math]::Min([math]::Max([math]::Ceiling($elapsedValues.Count * 0.99) - 1, 0), $elapsedValues.Count - 1)

    $p95 = $elapsedValues[$p95Index]
    $p99 = $elapsedValues[$p99Index]

    [PSCustomObject]@{
        Total   = $total
        Success = $ok
        Failed  = $failed
        AvgMs   = $avg
        MinMs   = $min
        MaxMs   = $max
        P95Ms   = $p95
        P99Ms   = $p99
    }
}

function Write-Summary {
    param(
        [string]$Path,
        [object]$Summary,
        [string]$SelectedTestType,
        [string]$JtlPath,
        [string]$LogPath
    )

    $assessment = "FAILED"
    if ($Summary.AvgMs -lt 500 -and $Summary.Failed -eq 0) {
        $assessment = "PASSED"
    } elseif ($Summary.AvgMs -lt 1000 -and $Summary.Failed -le [math]::Max(1, [math]::Floor($Summary.Total * 0.05))) {
        $assessment = "ACCEPTABLE"
    }

    $content = @"
LearnEz JMeter Test Summary
===========================
Test Type: $SelectedTestType
Date: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
JTL File: $JtlPath
Log File: $LogPath

Results:
--------
Total Requests: $($Summary.Total)
Successful: $($Summary.Success)
Failed: $($Summary.Failed)

Response Time (ms):
  Average: $($Summary.AvgMs)
  Min: $($Summary.MinMs)
  Max: $($Summary.MaxMs)
  95th Percentile: $($Summary.P95Ms)
  99th Percentile: $($Summary.P99Ms)

Performance Assessment: $assessment
"@

    Set-Content -Path $Path -Value $content -Encoding UTF8
}

try {
    Write-Section "LearnEz JMeter Test Automation"

    if (-not (Test-Path $TestPlan)) {
        throw "Test plan not found: $TestPlan"
    }

    Ensure-Directory -Path $ResultsDir
    Ensure-Directory -Path $ReportsDir

    $jmeterBat = Resolve-JMeterBat
    Write-Host "Using JMeter: $jmeterBat" -ForegroundColor Green
    Write-Host "Using test plan: $TestPlan" -ForegroundColor Green

    $jmeterArgs = @(
        "-n",
        "-t", $TestPlan,
        "-l", $JtlFile,
        "-j", $LogFile
    )

    if ($TestType -eq "Load") {
        $jmeterArgs += @("-JrunLoad=true", "-JrunStress=false")
    } elseif ($TestType -eq "Stress") {
        $jmeterArgs += @("-JrunLoad=false", "-JrunStress=true")
    } else {
        $jmeterArgs += @("-JrunLoad=true", "-JrunStress=false")
    }

    Write-Host "Running test..." -ForegroundColor Cyan
    & $jmeterBat @jmeterArgs

    if ($LASTEXITCODE -ne 0) {
        throw "JMeter exited with code $LASTEXITCODE"
    }

    $summary = Parse-JtlSummary -Path $JtlFile

    Write-Section "Execution Summary"
    Write-Host "Total requests : $($summary.Total)"
    Write-Host "Success        : $($summary.Success)" -ForegroundColor Green
    Write-Host "Failed         : $($summary.Failed)" -ForegroundColor $(if ($summary.Failed -gt 0) { "Red" } else { "Green" })
    Write-Host "Average (ms)   : $($summary.AvgMs)"
    Write-Host "P95 (ms)       : $($summary.P95Ms)"
    Write-Host "P99 (ms)       : $($summary.P99Ms)"

    if ($GenerateReport) {
        Ensure-Directory -Path $ReportDir
        Write-Host "Generating HTML report..." -ForegroundColor Cyan
        & $jmeterBat "-g" $JtlFile "-o" $ReportDir

        if ($LASTEXITCODE -ne 0) {
            throw "Failed to generate HTML report"
        }

        $summaryPath = Join-Path $ReportDir "summary.txt"
        Write-Summary -Path $summaryPath -Summary $summary -SelectedTestType $TestType -JtlPath $JtlFile -LogPath $LogFile

        if ($OpenReport) {
            $indexPath = Join-Path $ReportDir "index.html"
            if (Test-Path $indexPath) {
                Start-Process $indexPath
            }
        }

        Write-Host "Report folder: $ReportDir" -ForegroundColor Yellow
    }

    Write-Host "JTL result: $JtlFile" -ForegroundColor Yellow
    Write-Host "Log file  : $LogFile" -ForegroundColor Yellow
    Write-Host "Done." -ForegroundColor Green
}
catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
