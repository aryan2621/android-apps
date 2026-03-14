import datetime


class create_todo_dto:
    def __init__(
        self,
        title: str,
        user_id: str,
        task_id: str,
        description: str = None,
        done: bool = False,
        image_url: str = None,
        created_at: datetime.datetime = datetime.datetime.now(),
    ):
        if not title:
            raise ValueError("Title is required")
        if not user_id:
            raise ValueError("A valid user_id is required")
        if not task_id:
            raise ValueError("A valid task_id is required")
        self.task_id = task_id
        self.user_id = user_id
        self.title = title
        self.description = description
        self.done = done
        self.image_url = image_url
        self.created_at = str(created_at)


class update_todo_by_id_dto:
    def __init__(
        self,
        task_id: str,
        user_id: str,
        title: str = None,
        description: str = None,
        done: bool = None,
        image_url: str = None,
        created_at: datetime.datetime = datetime.datetime.now(),
    ):
        if not task_id:
            raise ValueError("A valid task_id is required")
        if not user_id:
            raise ValueError("A valid user_id is required")
        self.title = title
        self.task_id = task_id
        self.description = description
        self.done = done
        self.image_url = image_url
        self.user_id = user_id,
        self.created_at = str(created_at)


class delete_todo_by_id_dto:
    def __init__(self, task_id: str):
        if not task_id:
            raise ValueError("A valid task_id is required")
        self.task_id = task_id


class register_user_dto:
    def __init__(
        self,
        name: str,
        email: str,
        password: str,
        user_id: str = None,
        image_url: str = None,
        created_at: datetime.datetime = datetime.datetime.now(),
    ):
        if not name:
            raise ValueError("Name is required")
        if not email:
            raise ValueError("Email is required")
        if not password:
            raise ValueError("password is required")
        if not user_id:
            raise ValueError("A valid user_id is required")
        self.user_id = user_id
        self.name = name
        self.email = email
        self.password = password
        self.image_url = image_url
        self.created_at = str(created_at)


class login_user_dto:
    def __init__(self, email: str, password: str):
        if not email:
            raise ValueError("Email is required")
        if not password:
            raise ValueError("password is required")
        self.email = email
        self.password = password
