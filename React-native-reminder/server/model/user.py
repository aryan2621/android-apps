class User:
    def __init__(self, user_id, name, email, password, created_at=None, image_url=None):
        self.user_id = user_id
        self.name = name
        self.email = email
        self.password = password
        self.image_url = image_url
        self.created_at = created_at

    def serialize(self):
        return {
            "user_id": self.user_id,
            "name": self.name,
            "email": self.email,
            "password": self.password,
            "image_url": self.image_url,
            "created_at": self.created_at,
        }
