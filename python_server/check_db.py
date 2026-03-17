from database import SessionLocal, engine
from sqlalchemy import inspect, text

db = SessionLocal()
inspector = inspect(engine)

# invoice_files structure + contents
print("=== invoice_files columns ===")
for c in inspector.get_columns("invoice_files"):
    print(f"  {c['name']}: {c['type']}")
print("\n=== invoice_files data ===")
for r in db.execute(text('SELECT * FROM invoice_files')).fetchall():
    print(dict(r._mapping))

print("\n=== invoices full data ===")
for r in db.execute(text('SELECT * FROM invoices')).fetchall():
    print(dict(r._mapping))

import sys; sys.exit(0)

tables = inspector.get_table_names()
print("All tables:", tables)
for t in tables:
    count = db.execute(text(f'SELECT COUNT(*) FROM "{t}"')).scalar()
    print(f"  {t}: {count} rows")

# Check OCR-related tables if any
for t in tables:
    if "ocr" in t.lower() or "extract" in t.lower() or "upload" in t.lower():
        print(f"\n--- {t} contents ---")
        rows = db.execute(text(f'SELECT * FROM "{t}" LIMIT 5')).fetchall()
        for r in rows:
            print(" ", dict(r._mapping))

# Also check if vendor name is stored anywhere else
print("\n--- Searching for SUBAM in all text columns ---")
for t in tables:
    try:
        cols = [c["name"] for c in inspector.get_columns(t) if "char" in str(c["type"]).lower() or "text" in str(c["type"]).lower()]
        for col in cols:
            rows = db.execute(text(f'SELECT * FROM "{t}" WHERE LOWER("{col}") LIKE \'%subam%\' LIMIT 3')).fetchall()
            if rows:
                print(f"  Found in {t}.{col}:")
                for r in rows:
                    print("   ", dict(r._mapping))
    except Exception as e:
        print(f"  Error checking {t}: {e}")

db.close()
