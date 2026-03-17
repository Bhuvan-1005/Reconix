"""
routers/dashboard.py - Dashboard metrics and activity feed
GET /dashboard/metrics
GET /dashboard/activity
"""
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from core.security import finance_or_admin
from database import get_db
from services import finance_service

router = APIRouter(prefix="/dashboard", tags=["dashboard"])


@router.get("/metrics")
def get_metrics(db: Session = Depends(get_db), _user: dict = Depends(finance_or_admin)):
    """GET /dashboard/metrics – Finance dashboard summary statistics."""
    metrics = finance_service.get_dashboard_metrics(db)
    return metrics.model_dump()


@router.get("/activity")
def get_activity(
    limit: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
    _user: dict = Depends(finance_or_admin),
):
    """GET /dashboard/activity – Recent invoice action activity feed."""
    activity = finance_service.get_recent_activity(db, limit=limit)
    return [a.model_dump() for a in activity]
