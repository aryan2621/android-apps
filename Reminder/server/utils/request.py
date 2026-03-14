from server.utils.exception import (
    NotFoundException,
    InvalidRequestException,
    UnauthorizedException,
)
import datetime
import jwt
from flask import request, jsonify
from functools import wraps

secret_key = "supersecretkey"


def get_status_code(e):
    if isinstance(e, NotFoundException):
        return 404
    if isinstance(e, InvalidRequestException):
        return 400
    if isinstance(e, UnauthorizedException):
        return 401
    if isinstance(e, ValueError):
        return 400
    return 500


def encode_token(user_id):
    payload = {
        "user_id": user_id,
        "exp": datetime.datetime.utcnow()
        + datetime.timedelta(days=1),  # Token expires in 1 day
    }
    return jwt.encode(payload, secret_key, algorithm="HS256")


def decode_token(token):
    try:
        payload = jwt.decode(token, secret_key, algorithms=["HS256"])
        return payload["user_id"]
    except jwt.ExpiredSignatureError:
        return None
    except jwt.InvalidTokenError:
        return None


def auth(request):
    auth_header = request.headers.get("Authorization")
    if auth_header:
        token = auth_header.split(" ")[1]
        return decode_token(token)
    return None


def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        user_id = auth(request)
        if user_id is None:
            return jsonify({"error": "Unauthorized"}), 401
        return f(*args, **kwargs)

    return decorated_function
