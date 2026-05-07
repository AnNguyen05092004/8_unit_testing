from pathlib import Path
import sys
sys.path.append(r'L:\LearnEz')
from TestTool.SystemAutomation.learn_ez_system_automation import AutomationRunner
r=AutomationRunner(Path(r'L:\LearnEz\TestTool\SystemAutomation\reports\_tmp_remaining'))
r._choose_sample_data()
checks=[
    ('TC_UI_080', lambda: r._ui_case_schedule_edit_mode()),
    ('TC_UI_081', lambda: r._ui_case_schedule_cancel_confirmation()),
    ('TC_FN_022', lambda: r._api_case_create_recurring_schedule()),
    ('TC_FN_024', lambda: r._api_case_update_schedule()),
    ('TC_FN_068', lambda: r._api_case_update_schedule_contract()),
]
for code, fn in checks:
    try:
        print(code, '=>', fn())
    except Exception as e:
        print(code, 'ERROR =>', repr(e))
r.close()
