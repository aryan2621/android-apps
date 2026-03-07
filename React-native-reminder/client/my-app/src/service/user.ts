import ky from 'ky';
import { getAuth } from '../storage';
import { URL } from '../store/auth';
import { v4 as uuidv4 } from 'uuid';

const CREATE_REQ = {
    INPUT: {
        NAME: 'name',
        EMAIL: 'email',
        PASSWORD: 'password',
        USER_ID: 'user_id',
    },
};

export const signUp = (data: any) => {
    try {
        const input = {
            name: data[CREATE_REQ.INPUT.NAME],
            email: data[CREATE_REQ.INPUT.EMAIL],
            password: data[CREATE_REQ.INPUT.PASSWORD],
            user_id: uuidv4(),
        };
        return ky
            .post(`${URL}/register`, {
                json: input,
            })
            .json();
    } catch (error) {
        console.log(`Failed to create user: ${error}`);
    }
};
export const signIn = (data: any) => {
    const input = {
        email: data[CREATE_REQ.INPUT.EMAIL],
        password: data[CREATE_REQ.INPUT.PASSWORD],
    };
    return ky
        .post(`${URL}/login`, {
            json: input,
        })
        .json();
};

export const getUser = async () => {
    try {
        const token = await getAuth();
        return ky
            .get(`${URL}/user`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .json();
    } catch (error) {
        console.log(`Failed to fetch users: ${error}`);
    }
};

export const updateUser = async (image_url: string) => {
    try {
        const token = await getAuth();
        return ky
            .put(`${URL}/update_user`, {
                json: {
                    image_url: image_url,
                },
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .json();
    } catch (error) {
        console.log(`Failed to update user: ${error}`);
    }
};
