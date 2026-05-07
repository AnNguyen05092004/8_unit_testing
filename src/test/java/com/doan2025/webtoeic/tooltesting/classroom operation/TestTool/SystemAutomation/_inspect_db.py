import mysql.connector

conn = mysql.connector.connect(host='localhost', port=3306, database='SQL', user='root', password='987choithoi')
cur = conn.cursor(dictionary=True)

queries = {
    'rooms': "SELECT id, name, is_active, is_delete FROM room WHERE name IN ('Room C303') OR id IN (SELECT room FROM class_schedule WHERE title LIKE 'Weekend Session%') ORDER BY id",
    'classes': "SELECT id, name, title, status, teacher, created_at FROM class WHERE name LIKE 'TOEIC Offline Weekend C1' OR title LIKE 'Offline Weekend Batch C1' OR title LIKE 'Weekend%' ORDER BY id",
    'schedules': "SELECT id, class, room, title, status, is_active, is_delete, start_at, end_at FROM class_schedule WHERE title LIKE 'Weekend Session%' ORDER BY id",
    'schedule_counts': "SELECT c.id, c.name, c.title, COUNT(s.id) AS schedules FROM class c LEFT JOIN class_schedule s ON s.class = c.id AND s.is_delete = 0 WHERE c.name LIKE 'TOEIC Offline Weekend C1' OR c.title LIKE 'Offline Weekend Batch C1' GROUP BY c.id, c.name, c.title ORDER BY c.id",
    'members': "SELECT c.name, u.email, cm.role_in_class, cm.status FROM class_member cm JOIN class c ON c.id = cm.class JOIN user u ON u.id = cm.member WHERE c.name LIKE 'TOEIC Offline Weekend C1' ORDER BY cm.id",
}

for name, sql in queries.items():
    print(f'-- {name} --')
    cur.execute(sql)
    rows = cur.fetchall()
    for row in rows:
        print(row)
    if not rows:
        print('(no rows)')

cur.close()
conn.close()
