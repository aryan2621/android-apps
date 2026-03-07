import { create } from 'zustand';
import { persist, devtools, createJSONStorage } from 'zustand/middleware';
import { getAuth, setAuth, setUser } from '../storage';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { DBUser } from '../model';

export const URL = 'http://192.168.100.3:5000/';

interface AuthStore {
    isLoggedIn: boolean;
    login: () => void;
    logout: () => void;

    currentUser: DBUser;
    setCurrentUser: (user: any) => void;
    removeCurrentUser: () => void;
}
const useAuthStore = create(
    devtools(
        persist<AuthStore>(
            (set) => ({
                isLoggedIn: false,
                login: async () => {
                    const userLocalStorage = await getAuth();
                    if (userLocalStorage) {
                        set({ isLoggedIn: true });
                    }
                },
                logout: async () => {
                    set({ isLoggedIn: false });
                    await setAuth('');
                },
                currentUser: new DBUser({}),
                setCurrentUser: async (user) => {
                    set({ currentUser: user });
                    await setUser(user);
                },
                removeCurrentUser: async () => {
                    set({ currentUser: undefined });
                    await setUser('');
                },
            }),
            {
                name: 'userLoginStatus',
                storage: createJSONStorage(() => AsyncStorage),
            }
        )
    )
);

export default useAuthStore;
