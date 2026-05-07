import mysql.connector
from collections import defaultdict

conn = mysql.connector.connect(host='localhost', port=3306, database='SQL', user='root', password='987choithoi')
cur = conn.cursor(dictionary=True)

cur.execute("""
SELECT r.id, r.name, COUNT(s.id) AS schedule_count,
       MIN(s.start_at) AS first_start,
       MAX(s.end_at) AS last_end
FROM room r
LEFT JOIN class_schedule s ON s.room = r.id AND s.is_delete = 0
WHERE r.is_delete = 0
GROUP BY r.id, r.name
ORDER BY schedule_count ASC, r.id ASC
LIMIT 20
""")
print('-- room_load --')
for row in cur.fetchall():
    print(row)

cur.execute("""
SELECT id, name, description, is_active, is_delete
FROM room
WHERE is_delete = 0
ORDER BY id ASC
LIMIT 20
""")
print('-- rooms --')
for row in cur.fetchall():
    print(row)

cur.close()
conn.close()
