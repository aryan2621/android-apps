# Todo App

A mobile application to create a personalized Todo List with support for adding images.

[![Todo Demo](https://markdown-videos-api.jorgenkh.no/url?url=https%3A%2F%2Fyoutu.be%2FFFQYnk6LRko)](https://youtu.be/FFQYnk6LRko)

---

## Tech Stack

**Client:**  
- React Native  
- Zustand  
- JWT Authentication  
- Styled Components  
- Firebase  

**Server:**  
- Flask  
- SQLAlchemy  
- MySQL  

---

## Environment Variables

To run this project, add the following environment variables to your `.env` file:

- `FIRE_BASE_API_S`  
- `MY_SQL_SERVER`  

---

## Features

- **User Authentication**: Sign-up and login with JWT  
- **Personalized Todo Lists**: Todos are specific to individual users  
- **CRUD Operations**: Create, Read, Update, and Delete todos  
- **Camera Integration**: Attach images to todos using your device camera  
- **User Profiles**: Manage personal user profiles  

---

## API Reference

### Todo Endpoints

- **GET** `/todos`: Fetch all todos  
- **POST** `/todos`: Create a new todo  
- **DELETE** `/deleteTodoById`: Remove a todo by ID  
- **PUT** `/updateTodoById`: Update a todo by ID  

**Headers**  
- `TOKEN`: **string** (Required) – JWT for authorization  

### User Endpoints

- **GET** `/User`: Get user details  
- **PUT** `/updateUser`: Update user information  
- **POST** `/register`: Register a new user  
- **POST** `/login`: Login with existing credentials  

---

## Contributing

Currently, this project is authored by me. If you'd like to contribute, feel free to reach out at:  
**Email**: risha2621@gmail.com  
