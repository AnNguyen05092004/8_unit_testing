import mysql.connector

conn = mysql.connector.connect(host='localhost', port=3306, database='SQL', user='root', password='987choithoi')
conn.autocommit = True
cur = conn.cursor(dictionary=True)

updates = [
    """
    UPDATE class_schedule
    SET room = 2,
        start_at = DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 8 HOUR,
        end_at = DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 11 HOUR,
        status = 'ACTIVE',
        is_active = 1,
        is_delete = 0
    WHERE id = 4
    """,
    """
    UPDATE class_schedule
    SET room = 2,
        start_at = DATE_ADD(CURDATE(), INTERVAL 8 DAY) + INTERVAL 8 HOUR,
        end_at = DATE_ADD(CURDATE(), INTERVAL 8 DAY) + INTERVAL 11 HOUR,
        status = 'ACTIVE',
        is_active = 1,
        is_delete = 0
    WHERE id = 5
    """,
]

for sql in updates:
    cur.execute(sql)

cur.execute("SELECT id, class, room, title, status, is_active, is_delete, start_at, end_at FROM class_schedule WHERE id IN (4,5) ORDER BY id")
print('UPDATED_SCHEDULES')
for row in cur.fetchall():
    print(row)

cur.close()
conn.close()
