from flask import Blueprint
from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi
from flask import request, jsonify
from server.utils.exception import (
    NotFoundException,
)
from server.utils.request import (
    get_status_code,
    auth,
    login_required,
)
from bson import json_util
import json

# Credentials
user_name = "rishabh"
password = "aryan2621"
cluster_name = "cluster0"
mongo_url = f"mongodb+srv://{user_name}:{password}@{cluster_name}.rshleo8.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
# Mongo Client
client = MongoClient(
    mongo_url, server_api=ServerApi("1"), uuidRepresentation="standard"
)
user_app = Blueprint("user", __name__)


# Collection
def get_collection(collection_name):
    try:
        database = client.get_database("reminder")
        collection = database.get_collection(collection_name)
        return collection
    except Exception as e:
        raise e


@user_app.route("/update_user", methods=["PUT"])
@login_required
def update_user():
    try:
        user_id = auth(request)
        users = get_collection("users")
        user = users.find_one({"user_id": user_id})
        if not user:
            raise NotFoundException("User not found")
        image_url = request.json.get("image_url")
        users.update_one({"user_id": user_id}, {"$set": {"image_url": image_url}})
        return jsonify({"message": "User updated"}), 200
    except Exception as e:
        return jsonify({"error": f"Error updating user: {e}"}), get_status_code(e)


@user_app.route("/user", methods=["GET"])
@login_required
def get_user():
    try:
        user_id = auth(request)
        users = get_collection("users")
        user = users.find_one({"user_id": user_id})
        return jsonify(json.loads(json_util.dumps(user))), 200
    except Exception as e:
        return jsonify({"error": f"Error fetching user: {e}"}), get_status_code(e)
