import 'react-native-get-random-values';
import { v4 as uuidv4 } from 'uuid';
export class SignInUser {
    email: string | undefined;
    password: string | undefined;

    constructor(json: Partial<SignInUser>) {
        this.email = json.email || '';
        this.password = json.password || '';
    }
}
export class SignUpUser {
    name: string | undefined;
    email: string | undefined;
    password: string | undefined;
    confirmPassword: string | undefined;

    constructor(json: Partial<SignUpUser>) {
        this.name = json.name || '';
        this.email = json.email || '';
        this.password = json.password || '';
        this.confirmPassword = json.confirmPassword || '';
    }
}
export class DBTask {
    taskId: string | undefined;
    title: string | undefined;
    description: string | undefined;
    done: boolean | undefined;
    imageUrl: string | undefined;
    createdAt: string | undefined;
    userId: string | undefined;

    constructor(json: Partial<any>) {
        this.taskId = json.task_id || uuidv4();
        this.title = json.title;
        this.description = json.description;
        this.done = json.done || false;
        this.imageUrl = json.image_url;
        this.createdAt = json.created_at ?? new Date().toISOString();
        this.userId = json.user_id;
    }
}
export class DBUser {
    userId: string | undefined;
    name: string | undefined;
    email: string | undefined;
    imageUrl: string | undefined;
    password: string | undefined;
    createdAt: string | undefined;

    constructor(json: Partial<any>) {
        this.userId = json.user_id || uuidv4();
        this.name = json.name;
        this.email = json.email;
        this.password = json.password;
        this.imageUrl = json.image_url;
        this.createdAt = json.created_at ?? new Date().toISOString();
    }
}
