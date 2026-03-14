import AsyncStorage from '@react-native-async-storage/async-storage';
import { DBUser } from '../model';

export const setAuth = async (auth: string) => {
    try {
        await AsyncStorage.setItem('auth', auth);
    } catch (error) {
        console.log(`Error while setting auth: ${error}`);
    }
};

export const getAuth = async () => {
    try {
        return (await AsyncStorage.getItem('auth')) || '';
    } catch (error) {
        console.log(`Error while getting auth: ${error}`);
        return '';
    }
};

export const setUser = async (user: any) => {
    try {
        await AsyncStorage.setItem('user', JSON.stringify(user));
    } catch (error) {
        console.log(`Error while setting user: ${error}`);
    }
};

export const getUser = async () => {
    try {
        const user = await AsyncStorage.getItem('user');
        return user ? JSON.parse(user) : '';
    } catch (error) {
        console.log(`Error while getting user: ${error}`);
        return null;
    }
};
