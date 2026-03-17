"""
services/auth_service.py - Authentication logic
Equivalent to server/.../service/AuthService.kt
Uses passlib[bcrypt] + PyJWT via core.security for proper token generation.
"""
import hashlib
from datetime import datetime, timezone
from typing import Optional

from sqlalchemy.orm import Session

from core.security import create_access_token, verify_password
from models import User
from schemas import LoginRequest, LoginResponse, UserDTO


def _hash_password(password: str) -> str:
    """SHA-256 hash – kept for legacy seed-data compatibility only."""
    return hashlib.sha256(password.encode()).hexdigest()


def _generate_token(user_id: int, username: str, role: str) -> str:
    """Return a signed JWT containing user_id, username (sub) and role."""
    return create_access_token({"sub": username, "user_id": user_id, "role": role})


def login(db: Session, request: LoginRequest) -> LoginResponse:
    user = (
        db.query(User)
        .filter(User.username == request.username, User.is_active == True)
        .first()
    )

    # verify_password handles both legacy SHA-256 and modern bcrypt hashes
    if not user or not verify_password(request.password, user.password_hash):
        return LoginResponse(
            success=False,
            message="Invalid username or password",
            user=None,
            token=None,
        )

    # Update last login timestamp
    user.last_login_at = datetime.now(timezone.utc).isoformat()
    db.commit()

    user_dto = UserDTO(
        id=user.id,
        username=user.username,
        fullName=user.full_name,
        email=user.email,
        role=user.role,
        vendorId=user.vendor_id if hasattr(user, "vendor_id") else None,
    )
    token = _generate_token(user.id, user.username, user.role)
    return LoginResponse(
        success=True,
        message="Login successful",
        user=user_dto,
        token=token,
    )


def validate_credentials(db: Session, username: str, password: str) -> bool:
    user = db.query(User).filter(User.username == username, User.is_active == True).first()
    return user is not None and verify_password(password, user.password_hash)


def get_user_by_username(db: Session, username: str) -> Optional[UserDTO]:
    user = db.query(User).filter(User.username == username).first()
    if not user:
        return None
    return UserDTO(
        id=user.id,
        username=user.username,
        fullName=user.full_name,
        email=user.email,
        role=user.role,
        vendorId=user.vendor_id if hasattr(user, 'vendor_id') else None,
    )
