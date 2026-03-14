import { DBTask } from '../model';
import { getAuth } from '../storage';
import { URL } from '../store/auth';
import ky from 'ky';

const api = ky.create({
    prefixUrl: URL,
    hooks: {
        beforeRequest: [
            async (request) => {
                const auth = await getAuth();
                if (auth) {
                    request.headers.set('Authorization', `Bearer ${auth}`);
                }
            },
        ],
    },
});

export const fetchTodos = async () => {
    try {
        return await api.get('todos').json();
    } catch (error: any) {
        console.log(`Failed to fetch todos: ${error.message}`);
        throw new Error('Failed to fetch todos');
    }
};

export const createTodo = async (data: DBTask) => {
    try {
        const input = {
            title: data.title,
            task_id: data.taskId,
            description: data.description,
            done: data.done,
            image_url: data.imageUrl,
        };
        const response = await api.post('todos', { json: input }).json();
        return response;
    } catch (error: any) {
        console.log(`Failed to create todo: ${error.message}`);
        throw new Error('Failed to create todo');
    }
};

export const updateTodoById = async (id: string, data: DBTask) => {
    try {
        const input = {
            title: data.title,
            description: data.description,
            done: data.done,
            image_url: data.imageUrl,
        };
        const response = await api.put(`todos/${id}`, { json: input }).json();
        return response;
    } catch (error: any) {
        console.log(`Failed to update todo by id: ${error?.message}`);
        throw new Error('Failed to update todo by id');
    }
};

export const deleteTodoById = async (id: string) => {
    try {
        const response = await api.delete(`todos/${id}`).json();
        return response;
    } catch (error: any) {
        console.log(`Failed to delete todo by id: ${error.message}`);
        throw new Error('Failed to delete todo by id');
    }
};
