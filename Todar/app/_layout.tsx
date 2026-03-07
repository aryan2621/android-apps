import { Stack, useRouter } from "expo-router";
import React, { useEffect } from "react";
import {
    requestNotificationPermissions,
    setupNotificationListener,
} from "../utils/notifications";
import { ThemeProvider } from "../contexts/ThemeContext";

export default function Layout() {
    const router = useRouter();

    useEffect(() => {
        requestNotificationPermissions();
        const subscription = setupNotificationListener(async (habitId) => {
            router.push(`/task-completion?habitId=${habitId}`);
        });
        return () => {
            subscription.remove();
        };
    }, []);

    return (
        <ThemeProvider>
            <Stack screenOptions={{ headerShown: false }}>
                <Stack.Screen name="index" />
                <Stack.Screen name="select-habits" />
                <Stack.Screen name="configure-notifications" />
                <Stack.Screen name="home" />
                <Stack.Screen name="task-completion" />
                <Stack.Screen name="create-habit" />
                <Stack.Screen name="settings" />
            </Stack>
        </ThemeProvider>
    );
}
