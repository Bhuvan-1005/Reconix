"""
show_db.py — Display all tables and their data from the invoice_db PostgreSQL database.
"""
import os
from sqlalchemy import create_engine, inspect, text

DB_HOST = os.getenv("DATABASE_HOST", "localhost")
DB_PORT = os.getenv("DATABASE_PORT", "5433")
DB_NAME = os.getenv("DATABASE_NAME", "invoice_db")
DB_USER = os.getenv("DATABASE_USER", "postgres")
DB_PASS = os.getenv("DATABASE_PASSWORD", "postgres")
DATABASE_URL = f"postgresql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"

engine = create_engine(DATABASE_URL)

CYAN    = "\033[36m"
GREEN   = "\033[32m"
YELLOW  = "\033[33m"
BOLD    = "\033[1m"
RESET   = "\033[0m"
DIM     = "\033[2m"

def main():
    inspector = inspect(engine)
    tables = sorted(inspector.get_table_names())

    print(f"\n{BOLD}{CYAN}{'═' * 70}{RESET}")
    print(f"{BOLD}{CYAN}  📦  DATABASE: {DB_NAME}  (PostgreSQL @ {DB_HOST}:{DB_PORT}){RESET}")
    print(f"{BOLD}{CYAN}{'═' * 70}{RESET}")
    print(f"{DIM}  Tables found: {len(tables)}{RESET}\n")

    with engine.connect() as conn:
        for table in tables:
            # Get row count
            count = conn.execute(text(f'SELECT COUNT(*) FROM "{table}"')).scalar()

            print(f"{BOLD}{GREEN}┌─── 📋  {table.upper()}  ({count} row{'s' if count != 1 else ''}) ───{RESET}")

            if count == 0:
                print(f"{DIM}│  (empty table){RESET}")
                print(f"{GREEN}└{'─' * 60}{RESET}\n")
                continue

            # Fetch all rows
            result = conn.execute(text(f'SELECT * FROM "{table}"'))
            columns = list(result.keys())
            rows = result.fetchall()

            # Calculate column widths (cap at 30 chars per column)
            col_widths = []
            for i, col in enumerate(columns):
                max_w = len(col)
                for row in rows:
                    val = str(row[i]) if row[i] is not None else "NULL"
                    max_w = max(max_w, len(val))
                col_widths.append(min(max_w, 30))

            # Print header
            header = " │ ".join(f"{YELLOW}{col:<{col_widths[i]}}{RESET}" for i, col in enumerate(columns))
            print(f"│  {header}")
            separator = "─┼─".join("─" * w for w in col_widths)
            print(f"│  {DIM}{separator}{RESET}")

            # Print rows
            for row in rows:
                vals = []
                for i, val in enumerate(row):
                    s = str(val) if val is not None else f"{DIM}NULL{RESET}"
                    if len(s) > 30:
                        s = s[:27] + "..."
                    vals.append(f"{s:<{col_widths[i]}}")
                print(f"│  {' │ '.join(vals)}")

            print(f"{GREEN}└{'─' * 60}{RESET}\n")

    print(f"{BOLD}{CYAN}{'═' * 70}{RESET}\n")


if __name__ == "__main__":
    main()
