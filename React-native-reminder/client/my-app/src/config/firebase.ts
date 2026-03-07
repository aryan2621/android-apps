import { initializeApp, getApp, getApps } from 'firebase/app';
import firebase from 'firebase/app';
import { getStorage, ref as storageRef, uploadBytes, getDownloadURL } from 'firebase/storage';
import ky from 'ky';
import Config from 'react-native-config';

const getFirebaseApp = () => {
    const firebaseConfig = {
        apiKey: Config.EXPO_PUBLIC_API_KEY,
        authDomain: Config.EXPO_PUBLIC_AUTH_DOMAIN,
        projectId: Config.EXPO_PUBLIC_PROJECT_ID,
        storageBucket: Config.EXPO_PUBLIC_STORAGE_BUCKET,
        messagingSenderId: Config.EXPO_PUBLIC_MESSAGING_SENDER_ID,
        appId: Config.EXPO_PUBLIC_APP_ID,
    };

    let firebaseApp;
    if (getApps().length === 0) {
        firebaseApp = initializeApp(firebaseConfig);
    } else {
        firebaseApp = getApp();
    }
    return firebaseApp;
};
const fireStorage = getStorage(getFirebaseApp(), 'gs://reminder-bcc9b.appspot.com');

export const uploadImage = async (fileUri: string) => {
    try {
        const response = await fetch(fileUri);
        const blob = await response.blob();

        const r = storageRef(fireStorage, `images/${blob.size}_${new Date().getTime()}`);
        await uploadBytes(r, blob);
        const downloadURL = await getDownloadURL(r);
        return downloadURL;
    } catch (error) {
        console.log(`Failed to upload image: ${error}`);
        throw error;
    }
};

export const uploadProfile = async (fileUri: string) => {
    try {
        const response = await fetch(fileUri);
        const blob = await response.blob();
        const r = storageRef(fireStorage, `profile/${blob.size}_${new Date().getTime()}`);
        await uploadBytes(r, blob);
        const downloadURL = await getDownloadURL(r);
        return downloadURL;
    } catch (error) {
        console.log(`Failed to upload image: ${error}`);
        throw error;
    }
};

export const fetchImage = async (url: string) => {
    try {
        const blob = await ky.get(url).blob();
        const reader = new FileReader();
        reader.readAsDataURL(blob);
        return new Promise<string>((resolve, reject) => {
            reader.onloadend = () => {
                resolve(reader.result as string);
            };
        });
    } catch (error) {
        console.log(`Failed to fetch image: ${error}`);
        throw error;
    }
};
