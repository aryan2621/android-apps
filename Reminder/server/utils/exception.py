class NotFoundException(Exception):
    def __init__(self, message, status_code=404):
        self.message = message
        self.status_code = status_code
        super().__init__(self.message)

class InvalidRequestException(Exception):
    def __init__(self, message, status_code=400):
        self.message = message
        self.status_code = status_code
        super().__init__(self.message)
        
class UnauthorizedException(Exception):
    def __init__(self, message, status_code=401):
        self.message = message
        self.status_code = status_code
        super().__init__(self.message)