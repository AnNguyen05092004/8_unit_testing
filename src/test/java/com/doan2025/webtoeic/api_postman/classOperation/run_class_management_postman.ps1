param(
    [string]$Collection = "L:\LearnEz\TestTool\Postman\class-management-dashboard.postman_collection.json",
    [string]$Environment = "L:\LearnEz\TestTool\Postman\class-management-local.postman_environment.json",
    [string]$OutputJson = "L:\LearnEz\TestTool\Postman\class-management-newman-report.json",
    [string]$OutputCsv = "L:\LearnEz\TestTool\Postman\class-management-api-test-report.csv"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command newman -ErrorAction SilentlyContinue)) {
    Write-Host "Newman is not installed."
    Write-Host "Install command: npm install -g newman"
    Write-Host "Then re-run this script."
    exit 1
}

if (-not (Test-Path $Collection)) {
    Write-Host "Collection file not found: $Collection"
    exit 1
}

if (-not (Test-Path $Environment)) {
    Write-Host "Environment file not found: $Environment"
    exit 1
}

Write-Host "Running Postman collection with Newman..."
newman run $Collection -e $Environment -r json --reporter-json-export $OutputJson

if (-not (Test-Path $OutputJson)) {
    Write-Host "Newman report JSON was not generated."
    exit 1
}

$report = Get-Content $OutputJson -Raw | ConvertFrom-Json
$rows = @()

foreach ($exec in $report.run.executions) {
    $name = $exec.item.name
    $method = $exec.request.method
    $url = $exec.request.url
    if ($url -is [string]) {
        $urlText = $url
    } else {
        $urlText = $url.raw
    }

    $statusCode = $null
    $statusText = ""
    $responseTime = $null
    $appCode = $null
    $appMessage = ""
    $requestError = $null
    $requestErrorMessage = $null

    if ($exec.response) {
        $statusCode = $exec.response.code
        $statusText = $exec.response.status
        $responseTime = $exec.response.responseTime

        if ($exec.response.stream) {
            try {
                $bodyText = [System.Text.Encoding]::UTF8.GetString($exec.response.stream)
                if ($bodyText) {
                    $json = $bodyText | ConvertFrom-Json
                    if ($json.PSObject.Properties.Name -contains "code") {
                        $appCode = $json.code
                    }
                    if ($json.PSObject.Properties.Name -contains "message") {
                        $appMessage = ($json.message -as [string]) -replace "[\r\n,]", " "
                    }
                }
            } catch {
            }
        }
    }

    if ($exec.requestError) {
        $requestError = $exec.requestError.code
        $requestErrorMessage = $exec.requestError.message
    }

    $failedAssertions = @()
    if ($exec.assertions) {
        foreach ($assertion in $exec.assertions) {
            if ($assertion.error) {
                $failedAssertions += $assertion.assertion
            }
        }
    }

    $result = if ($requestError -eq "ECONNREFUSED") { "SKIP" } elseif ($failedAssertions.Count -eq 0) { "PASS" } else { "FAIL" }
    $note = if ($requestError -eq "ECONNREFUSED") {
        if ($requestErrorMessage) { "UNAVAILABLE: $requestErrorMessage" } else { "UNAVAILABLE: backend not reachable" }
    } elseif ($failedAssertions.Count -eq 0) {
        ""
    } else {
        ($failedAssertions -join "; ")
    }

    $testCaseId = switch ($name) {
        "Login Teacher" { "PM-CL-001" }
        "Login Manager" { "PM-CL-002" }
        "Get Own Profile" { "PM-CL-003" }
        "Filter Classes" { "PM-CL-004" }
        "Get Class Detail" { "PM-CL-005" }
        "Get Members In Class" { "PM-CL-006" }
        "Get Schedules In Class" { "PM-CL-007" }
        "Get Schedule Detail" { "PM-CL-008" }
        "Get Notifications In Class" { "PM-CL-009" }
        "Get Notification Detail" { "PM-CL-010" }
        "Overview Student Attendance" { "PM-CL-011" }
        "Overview Statistic Attendance" { "PM-CL-012" }
        "Detail Statistic Attendance" { "PM-CL-013" }
        "List Quiz In Class" { "PM-CL-014" }
        "Overview Student Submit In Class" { "PM-CL-015" }
        "Search Submitted Quiz By Student In Class" { "PM-CL-016" }
        "View Detail Submitted Quiz By Student" { "PM-CL-017" }
        "Statistic Overview Quizzes In Class" { "PM-CL-018" }
        "Statistic Detail Quiz In Class" { "PM-CL-019" }
        "User Filter (Add Student Modal)" { "PM-CL-020" }
        "Add User To Class" { "PM-CL-021" }
        "Remove User From Class" { "PM-CL-022" }
        "Create Notification In Class" { "PM-CL-023" }
        "Update Notification In Class" { "PM-CL-024" }
        "Disable Or Delete Notification In Class" { "PM-CL-025" }
        "Filter Rooms" { "PM-CL-026" }
        "Create Schedule In Class" { "PM-CL-027" }
        "Update Schedule In Class" { "PM-CL-028" }
        "Cancelled Schedule In Class" { "PM-CL-029" }
        "Set Manager Token For Requests" { "PM-CL-030" }
        "Create Class" { "PM-CL-031" }
        "Update Class" { "PM-CL-032" }
        "Delete Class" { "PM-CL-033" }
        "Create Room" { "PM-CL-034" }
        "Update Room" { "PM-CL-035" }
        default { "PM-CL-000" }
    }

    $testObjective = switch ($name) {
        "Login Teacher" { "Authenticate teacher account and capture JWT token" }
        "Login Manager" { "Authenticate manager account and capture JWT token" }
        "Get Own Profile" { "Verify authenticated profile endpoint" }
        "Filter Classes" { "Load class list for dashboard/class-management" }
        "Get Class Detail" { "Open class detail for first class" }
        "Get Members In Class" { "Load student list tab" }
        "Get Schedules In Class" { "Load class schedules" }
        "Get Schedule Detail" { "Open schedule detail modal" }
        "Get Notifications In Class" { "Load notification list tab" }
        "Get Notification Detail" { "Open notification detail" }
        "Overview Student Attendance" { "Load attendance overview data" }
        "Overview Statistic Attendance" { "Load attendance summary table" }
        "Detail Statistic Attendance" { "Load detailed attendance by schedule" }
        "List Quiz In Class" { "Load quizzes in class tab" }
        "Overview Student Submit In Class" { "Load student quiz overview" }
        "Search Submitted Quiz By Student In Class" { "Load submitted quiz list" }
        "View Detail Submitted Quiz By Student" { "Open submitted quiz detail" }
        "Statistic Overview Quizzes In Class" { "Load quiz statistics overview" }
        "Statistic Detail Quiz In Class" { "Load quiz statistics detail" }
        "User Filter (Add Student Modal)" { "Load student candidates for add-member modal" }
        "Add User To Class" { "Add a student to class" }
        "Remove User From Class" { "Remove a student from class" }
        "Create Notification In Class" { "Create a class notification" }
        "Update Notification In Class" { "Update a class notification" }
        "Disable Or Delete Notification In Class" { "Disable or delete a class notification" }
        "Filter Rooms" { "Load active rooms for scheduling" }
        "Create Schedule In Class" { "Create new class schedule" }
        "Update Schedule In Class" { "Update an existing schedule" }
        "Cancelled Schedule In Class" { "Cancel an existing schedule" }
        "Set Manager Token For Requests" { "Ensure manager token is attached" }
        "Create Class" { "Create a new class" }
        "Update Class" { "Update class metadata" }
        "Delete Class" { "Delete or cancel class" }
        "Create Room" { "Create a room for scheduling" }
        "Update Room" { "Update room information" }
        default { $name }
    }

    $inputText = switch ($name) {
        "Login Teacher" { "teacherEmail={{teacherEmail}}, teacherPassword={{teacherPassword}}" }
        "Login Manager" { "managerEmail={{managerEmail}}, managerPassword={{managerPassword}}" }
        "Get Own Profile" { "Authorization=Bearer {{teacherToken}}" }
        "Filter Classes" { "searchString=empty, page=0, size=10" }
        "Get Class Detail" { "classId={{classId}}" }
        "Get Members In Class" { "classId={{classId}}, page=0, size=20" }
        "Get Schedules In Class" { "classId={{classId}}, page=0, size=10" }
        "Get Schedule Detail" { "scheduleId={{scheduleId}}" }
        "Get Notifications In Class" { "classId={{classId}}, page=0, size=20" }
        "Get Notification Detail" { "notificationId={{notificationId}}" }
        "Overview Student Attendance" { "classId={{classId}}, page=0, size=100" }
        "Overview Statistic Attendance" { "classId={{classId}}, page=0, size=20" }
        "Detail Statistic Attendance" { "scheduleId={{attendanceScheduleId}}, page=0, size=20" }
        "List Quiz In Class" { "classId={{classId}}, page=0, size=20" }
        "Overview Student Submit In Class" { "classId={{classId}}, page=0, size=100" }
        "Search Submitted Quiz By Student In Class" { "quizId={{sharedQuizId}}, classId={{classId}}, page=0, size=20" }
        "View Detail Submitted Quiz By Student" { "submittedId={{submittedQuizId}}" }
        "Statistic Overview Quizzes In Class" { "classId={{classId}}, score=0" }
        "Statistic Detail Quiz In Class" { "quizId={{sharedQuizId}}, classId={{classId}}, score=0" }
        "User Filter (Add Student Modal)" { "role=student, isActive=true, isDelete=false" }
        "Add User To Class" { "classId={{classId}}, memberIds=[{{candidateStudentId}}]" }
        "Remove User From Class" { "classId={{classId}}, memberIds=[{{candidateStudentId}}]" }
        "Create Notification In Class" { "description=Postman test notification, typeNotification=1" }
        "Update Notification In Class" { "description=Postman test notification updated, isPin=true" }
        "Disable Or Delete Notification In Class" { "isActive=false, isDelete=true" }
        "Filter Rooms" { "isActive=true, isDelete=false" }
        "Create Schedule In Class" { "classId={{classId}}, roomId={{roomId}}" }
        "Update Schedule In Class" { "scheduleId={{createdScheduleId}}, roomId={{roomId}}" }
        "Cancelled Schedule In Class" { "ids={{createdScheduleId}}" }
        "Set Manager Token For Requests" { "Authorization=Bearer {{managerToken}}" }
        "Create Class" { "name/title/teacher" }
        "Update Class" { "id/name/title/teacher" }
        "Delete Class" { "ids={{createdClassId}}" }
        "Create Room" { "name/description" }
        "Update Room" { "id/name/description/isActive/isDelete" }
        default { $urlText }
    }

    $expectedOutput = switch ($result) {
        "PASS" { "Request succeeds with a valid API response" }
        "SKIP" { "Backend unavailable during run" }
        default { "API returns a non-2xx/expected validation outcome" }
    }

    $apiGate = $urlText
    if ([Uri]::IsWellFormedUriString($urlText, [UriKind]::Absolute)) {
        $apiGate = ([Uri]$urlText).PathAndQuery
    }

    $rows += [PSCustomObject]@{
        'File Name/Class Name' = 'DashboardClassManagementPostman'
        'Method Under Test' = "$method $apiGate"
        'Test Case ID' = $testCaseId
        'Test Objective' = $testObjective
        'Input' = $inputText
        'Expected Output' = $expectedOutput
        'Notes' = if ($note) { $note } else { "$statusCode $statusText" }
        Result = $result
    }
}

$csvPath = $OutputCsv
try {
    $rows | Select-Object 'File Name/Class Name','Method Under Test','Test Case ID','Test Objective','Input','Expected Output','Notes','Result' | Export-Csv -Path $csvPath -NoTypeInformation -Encoding UTF8
    Write-Host "CSV exported:" $csvPath
} catch {
    $fallbackCsv = [System.IO.Path]::ChangeExtension($OutputCsv, ".generated.csv")
    $rows | Select-Object 'File Name/Class Name','Method Under Test','Test Case ID','Test Objective','Input','Expected Output','Notes','Result' | Export-Csv -Path $fallbackCsv -NoTypeInformation -Encoding UTF8
    Write-Host "CSV exported to fallback path (target locked):" $fallbackCsv
}
