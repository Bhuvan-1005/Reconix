import sys
import traceback
import json

try:
    from database import SessionLocal
    from services.finance_service import get_pending_invoices
    db = SessionLocal()
    pending = get_pending_invoices(db)
    print("PENDING INVOICES:")
    for p in pending:
        print(json.dumps(p.model_dump(), indent=2))
except Exception as e:
    traceback.print_exc()
