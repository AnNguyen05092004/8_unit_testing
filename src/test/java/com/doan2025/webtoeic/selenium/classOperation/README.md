# LearnEz System Automation

This folder contains the data-driven automation runner for the classroom operation system tests.

## Run

```powershell
Set-Location L:\LearnEz\TestTool\SystemAutomation
.\run_system_automation.ps1
```

## Output

The runner writes:

- `system-automation-report_<timestamp>.csv`
- `system-automation-report_<timestamp>.html`
- `system-automation-summary_<timestamp>.md`

## Notes

- The runner reads `L:\LearnEz\system-test.csv` for case traceability.
- UI checks use Selenium WebDriver against the local FE.
- API and DB checks use the local Spring Boot backend and MySQL database.
- Temporary test data is rolled back by inverse API actions where possible.
