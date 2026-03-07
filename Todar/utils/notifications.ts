// utils/notifications.ts - FINAL CORRECT VERSION
import * as Device from "expo-device";
import * as Notifications from "expo-notifications";
import { Platform } from "react-native";
import { Habit } from "../types";
import { getTaskStatus, getTodayDate } from "./storage";

const DEFAULT_NOTIFICATION_SOUND = "notification.mp3";

Notifications.setNotificationHandler({
    handleNotification: async () => ({
        shouldShowBanner: true,
        shouldShowList: true,
        shouldPlaySound: true,
        shouldSetBadge: true,
    }),
});

export const requestNotificationPermissions = async (): Promise<boolean> => {
    if (!Device.isDevice) {
        console.log("Notifications only work on physical devices");
        return false;
    }

    if (__DEV__ && !Device.isDevice) {
        console.warn(
            "⚠️ Notifications have limited functionality in Expo Go. Consider using a development build for full notification support.",
        );
        return false;
    }

    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;

    if (existingStatus !== "granted") {
        const { status } = await Notifications.requestPermissionsAsync();
        finalStatus = status;
    }

    if (finalStatus !== "granted") {
        console.log("Notification permissions not granted");
        return false;
    }

    if (Platform.OS === "android") {
        await Notifications.setNotificationChannelAsync("habit-reminders", {
            name: "Habit Reminders",
            importance: Notifications.AndroidImportance.HIGH,
            vibrationPattern: [0, 250, 250, 250],
            lightColor: "#58CC02",
        });
    }

    return true;
};

export const scheduleHabitNotifications = async (habit: Habit): Promise<void> => {
    await cancelHabitNotifications(habit.id);
    
    for (const time of habit.notificationTimes) {
        const [hours, minutes] = time.split(":").map(Number);

        console.log(`Scheduling daily notification for ${habit.name} at ${time}`);

        try {
            const notificationId = await Notifications.scheduleNotificationAsync({
                content: {
                    title: "⚡ Time for your habit!",
                    body: `Don't forget to complete: ${habit.name}`,
                    data: { habitId: habit.id, habitName: habit.name },
                    sound: DEFAULT_NOTIFICATION_SOUND,
                },
                trigger: {
                    type: Notifications.SchedulableTriggerInputTypes.DAILY,
                    hour: hours,
                    minute: minutes,
                },
            });

            console.log(`Daily notification scheduled with ID: ${notificationId} at ${hours}:${minutes}`);
        } catch (error) {
            console.error(`Failed to schedule notification for ${habit.name}:`, error);
        }
    }
};

export const scheduleAllNotifications = async (habits: Habit[]): Promise<void> => {
    try {
        const hasPermission = await requestNotificationPermissions();
        if (!hasPermission) {
            console.warn("Notification permissions not granted or not available in current environment");
            return;
        }

        await Notifications.cancelAllScheduledNotificationsAsync();
        
        for (const habit of habits) {
            await scheduleHabitNotifications(habit);
        }
        
        console.log(`Successfully scheduled notifications for ${habits.length} habits`);
    } catch (error) {
        console.error("Error scheduling notifications:", error);
        console.warn("This might be due to Expo Go limitations. Consider using a development build for full notification support.");
    }
};

export const cancelHabitNotifications = async (habitId: string): Promise<void> => {
    try {
        const scheduledNotifications = await Notifications.getAllScheduledNotificationsAsync();

        for (const notification of scheduledNotifications) {
            if (notification.content.data?.habitId === habitId) {
                await Notifications.cancelScheduledNotificationAsync(notification.identifier);
            }
        }
    } catch (error) {
        console.error(`Failed to cancel notifications for habit ${habitId}:`, error);
    }
};

export const cancelAllNotifications = async (): Promise<void> => {
    try {
        await Notifications.cancelAllScheduledNotificationsAsync();
        console.log("All notifications cancelled");
    } catch (error) {
        console.error("Failed to cancel all notifications:", error);
    }
};

export const shouldNotifyHabit = async (habitId: string): Promise<boolean> => {
    const today = getTodayDate();
    const status = await getTaskStatus(habitId, today);
    return status === "pending";
};

export const setupNotificationListener = (
    onNotificationPress: (habitId: string) => void,
): Notifications.Subscription => {
    return Notifications.addNotificationResponseReceivedListener((response) => {
        const habitId = response.notification.request.content.data?.habitId;
        if (habitId) {
            onNotificationPress(habitId as string);
        }
    });
};

export const rescheduleNotificationsForTomorrow = async (habits: Habit[]): Promise<void> => {
    console.log("Using daily repeating notifications - no need to reschedule");
};

export const getScheduledNotifications = async (): Promise<Notifications.NotificationRequest[]> => {
    try {
        return await Notifications.getAllScheduledNotificationsAsync();
    } catch (error) {
        console.error("Failed to get scheduled notifications:", error);
        return [];
    }
};

export const debugScheduledNotifications = async (): Promise<void> => {
    try {
        const notifications = await getScheduledNotifications();
        console.log(`Total scheduled notifications: ${notifications.length}`);

        notifications.forEach((notification, index) => {
            const trigger = notification.trigger as any;
            console.log(`Notification ${index + 1}:`);
            console.log(`  - ID: ${notification.identifier}`);
            console.log(`  - Title: ${notification.content.title}`);
            console.log(`  - Body: ${notification.content.body}`);
            console.log(`  - Trigger type: ${trigger?.type}`);
            if (trigger?.type === Notifications.SchedulableTriggerInputTypes.DAILY) {
                console.log(`  - Time: ${trigger.hour}:${trigger.minute}`);
            }
            console.log(`  - Data:`, notification.content.data);
        });
    } catch (error) {
        console.error("Failed to debug notifications:", error);
    }
};