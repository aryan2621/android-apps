from flask import Flask
import os
from flask_bcrypt import Bcrypt
from flask import request, jsonify
from server.utils.dto import (
    register_user_dto,
    login_user_dto,
)
import json
from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi
from server.utils.exception import (
    InvalidRequestException,
    UnauthorizedException,
)
from server.utils.request import (
    get_status_code,
    encode_token,
)
from server.model.user import User


app = Flask(__name__)
app.config["SECRET_KEY"] = os.getenv("SECRET_KEY", "supersecretkey")

from server.routes.todo import todos_app
from server.routes.user import user_app

app.register_blueprint(todos_app)
app.register_blueprint(user_app)


# Credentials
user_name = "rishabh"
password = "aryan2621"
cluster_name = "cluster0"
mongo_url = f"mongodb+srv://{user_name}:{password}@{cluster_name}.rshleo8.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"

# Mongo Client
client = MongoClient(
    mongo_url, server_api=ServerApi("1"), uuidRepresentation="standard"
)
bcrypt = Bcrypt(app)


# Collection
def get_collection(collection_name):
    try:
        database = client.get_database("reminder")
        collection = database.get_collection(collection_name)
        return collection
    except Exception as e:
        raise e


@app.route("/register", methods=["POST"])
def register():
    try:
        dto = register_user_dto(
            name=request.json.get("name"),
            email=request.json.get("email"),
            password=request.json.get("password"),
        )
        users = get_collection("users")
        existing_user = users.find_one({"email": dto.email})
        if existing_user:
            raise InvalidRequestException("User already exists")

        users.insert_one(
            {
                "user_id": dto.user_id,
                "name": dto.name,
                "email": dto.email,
                "password": bcrypt.generate_password_hash(dto.password).decode("utf-8"),
                "image_url": "",
                "created_at": dto.created_at,
            }
        )
        return jsonify({"message": "User registered"}), 201
    except Exception as e:
        return jsonify({"error": "Error registering user"}), get_status_code(e)


@app.route("/login", methods=["POST"])
def login():
    try:
        email = request.json.get("email")
        password = request.json.get("password")
        dto = login_user_dto(email=email, password=password)

        users = get_collection("users")
        db_user = users.find_one({"email": dto.email})
        if db_user is None:
            raise UnauthorizedException("Invalid email or password")

        user = User(
            db_user["user_id"],
            db_user["name"],
            db_user["email"],
            db_user["password"],
            db_user.get("image_url"),
        )
        if not bcrypt.check_password_hash(user.password, dto.password):
            raise UnauthorizedException("Invalid email or password")

        token = encode_token(user.user_id)
        return jsonify({"token": token}), 200
    except Exception as e:
        return jsonify({"error": "Error logging in"}), get_status_code(e)


if __name__ == "__main__":
    app.run(port=5000, debug=True, threaded=True)
