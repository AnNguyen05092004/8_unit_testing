from pathlib import Path
import sys
sys.path.append(r'L:\LearnEz')
from TestTool.SystemAutomation.learn_ez_system_automation import AutomationRunner
r=AutomationRunner(Path(r'L:\LearnEz\TestTool\SystemAutomation\reports\_tmp_verify'))
try:
    r._choose_sample_data()
except Exception as e:
    print('choose_sample_data ERROR', repr(e))

checks=[
    ('TC_UI_025', lambda: r._ui_case_sidebar_route()),
    ('TC_UI_026', lambda: r._ui_case_row_actions_visible(r.context['sample_class']['id'])),
    ('TC_UI_039', lambda: r._ui_case_attendance_cta()),
    ('TC_UI_040', lambda: r._ui_case_attendance_modal_summary()),
    ('TC_UI_062', lambda: r._ui_case_refresh_schedule()),
    ('TC_UI_063', lambda: r._ui_case_overview_loading()),
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
