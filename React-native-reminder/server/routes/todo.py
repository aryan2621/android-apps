from flask import Blueprint
from server.utils.request import (
    get_status_code,
    auth,
    login_required,
)
from flask import request, jsonify
from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi
from server.utils.exception import (
    NotFoundException,
)
from server.utils.dto import (
    create_todo_dto,
    update_todo_by_id_dto,
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
todos_app = Blueprint("todo", __name__)


# Collection
def get_collection(collection_name):
    try:
        database = client.get_database("reminder")
        collection = database.get_collection(collection_name)
        return collection
    except Exception as e:
        raise e


@todos_app.route("/todos", methods=["GET"])
@login_required
def get_todos():
    try:
        user_id = auth(request)
        todos = get_collection("todos")
        todos = todos.find({"user_id": user_id})
        return jsonify(json.loads(json_util.dumps(todos))), 200
    except Exception as e:
        return jsonify({"error": f"Error fetching todos: {e}"}), get_status_code(e)


@todos_app.route("/todos", methods=["POST"])
@login_required
def create_todo():
    try:
        user_id = auth(request)
        dto = create_todo_dto(
            task_id=request.json.get("task_id"),
            user_id=user_id,
            title=request.json.get("title"),
            description=request.json.get("description"),
            done=request.json.get("done"),
            image_url=request.json.get("image_url"),
        )
        todos = get_collection("todos")
        todos.insert_one(
            {
                "task_id": dto.task_id,
                "title": dto.title,
                "description": dto.description,
                "done": dto.done,
                "image_url": dto.image_url,
                "user_id": user_id,
                "created_at": dto.created_at,
            }
        )
        return jsonify({"message": "Todo created"}), 201
    except Exception as e:
        return jsonify({"error": f"Error creating todo: {e}"}), get_status_code(e)


@todos_app.route("/todos/<string:id>", methods=["PUT"])
@login_required
def update_todo_by_id(id):
    try:
        user_id = auth(request)
        todos = get_collection("todos")
        todo = todos.find_one({"task_id": id})
        if not todo:
            raise NotFoundException("Todo not found")

        dto = update_todo_by_id_dto(
            task_id=id,
            user_id=user_id,
            title=request.json.get("title"),
            description=request.json.get("description"),
            done=request.json.get("done"),
            image_url=request.json.get("image_url"),
        )
        updates = {}
        if dto.title is not None:
            updates["title"] = dto.title
        if dto.description is not None:
            updates["description"] = dto.description
        if isinstance(dto.done, bool):
            updates["done"] = dto.done
        if dto.image_url is not None:
            updates["image_url"] = dto.image_url
        updates['created_at'] = dto.created_at
        todos.update_one({"task_id": id}, {"$set": updates})
        return jsonify({"message": "Todo updated"}), 200
    except Exception as e:
        return jsonify({"error": f"Error updating todo: {e}"}), get_status_code(e)


@todos_app.route("/todos/<string:id>", methods=["DELETE"])
@login_required
def delete_todo_by_id(id):
    try:
        todos = get_collection("todos")
        todo = todos.find_one({"task_id": id})
        if not todo:
            raise NotFoundException("Todo not found")

        todos.delete_one({"task_id": id})
        return jsonify({"message": "Todo deleted"}), 200
    except Exception as e:
        return jsonify({"error": f"Error deleting todo: {e}"}), get_status_code(e)
