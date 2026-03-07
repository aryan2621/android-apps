class Todo:
    def __init__(
        self, task_id, title, description, done, created_at, image_url, user_id
    ):
        self.task_id = task_id
        self.title = title
        self.description = description
        self.done = done
        self.created_at = created_at
        self.image_url = image_url
        self.user_id = user_id

    def __repr__(self):
        return f"<Todo {self.task_id}>"

    def serialize(self):
        return {
            "task_id": self.task_id,
            "title": self.title,
            "description": self.description,
            "done": self.done,
            "created_at": self.created_at,
            "image_url": self.image_url,
            "user_id": self.user_id,
        }
