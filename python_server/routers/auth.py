"""
routers/auth.py - Authentication routes
POST /auth/login
POST /auth/logout
"""
from fastapi import APIRouter, Depends, status
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session

from database import get_db
from schemas import ApiError, LoginRequest, LoginResponse
from services import auth_service

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/login", response_model=LoginResponse)
def login(request: LoginRequest, db: Session = Depends(get_db)):
    if not request.username.strip():
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content=LoginResponse(success=False, message="Username is required").model_dump(),
        )
    if not request.password.strip():
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content=LoginResponse(success=False, message="Password is required").model_dump(),
        )
    response = auth_service.login(db, request)
    status_code = status.HTTP_200_OK if response.success else status.HTTP_401_UNAUTHORIZED
    return JSONResponse(status_code=status_code, content=response.model_dump())


@router.post("/logout")
def logout():
    return {"success": True, "message": "Logged out successfully"}
