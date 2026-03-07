import AsyncStorage from "@react-native-async-storage/async-storage";
import { DailyStatus, Habit } from "../types";

const HABITS_KEY = "@todar_habits";
const DAILY_STATUS_KEY = "@todar_daily_status";

const isValidHabit = (habit: any): habit is Habit => {
    return (
        habit &&
        typeof habit.id === "string" &&
        typeof habit.name === "string" &&
        typeof habit.icon === "string" &&
        Array.isArray(habit.notificationTimes) &&
        typeof habit.streak === "number" &&
        (habit.lastCompleted === null || typeof habit.lastCompleted === "string") &&
        typeof habit.createdAt === "string"
    );
};

export const saveHabits = async (habits: Habit[]): Promise<void> => {
    try {
        await AsyncStorage.setItem(HABITS_KEY, JSON.stringify(habits));
    } catch (error) {
        console.error("Error saving habits:", error);
        throw new Error("Failed to save habits");
    }
};

export const getHabits = async (): Promise<Habit[]> => {
    try {
        const data = await AsyncStorage.getItem(HABITS_KEY);
        if (!data) return [];
        
        const parsed = JSON.parse(data);
        
        // Validate data
        if (!Array.isArray(parsed)) {
            console.warn("Invalid habits data format, resetting to empty array");
            return [];
        }
        
        // Filter out invalid habits
        const validHabits = parsed.filter((habit) => {
            const isValid = isValidHabit(habit);
            if (!isValid) {
                console.warn("Skipping invalid habit:", habit);
            }
            return isValid;
        });
        
        return validHabits;
    } catch (error) {
        console.error("Error getting habits:", error);
        // If JSON parse fails, return empty array instead of crashing
        return [];
    }
};

export const saveDailyStatus = async (status: DailyStatus): Promise<void> => {
    try {
        await AsyncStorage.setItem(DAILY_STATUS_KEY, JSON.stringify(status));
    } catch (error) {
        console.error("Error saving daily status:", error);
        throw new Error("Failed to save daily status");
    }
};

export const getDailyStatus = async (): Promise<DailyStatus> => {
    try {
        const data = await AsyncStorage.getItem(DAILY_STATUS_KEY);
        if (!data) return {};
        
        const parsed = JSON.parse(data);
        
        // Basic validation
        if (typeof parsed !== "object" || parsed === null || Array.isArray(parsed)) {
            console.warn("Invalid daily status format, resetting");
            return {};
        }
        
        return parsed;
    } catch (error) {
        console.error("Error getting daily status:", error);
        return {};
    }
};

export const getTodayDate = (): string => {
    const today = new Date();
    return today.toISOString().split("T")[0];
};

export const getTaskStatus = async (
    habitId: string,
    date: string,
): Promise<"done" | "skipped" | "pending"> => {
    try {
        const dailyStatus = await getDailyStatus();
        return dailyStatus[habitId]?.[date] || "pending";
    } catch (error) {
        console.error("Error getting task status:", error);
        return "pending";
    }
};

export const updateTaskStatus = async (
    habitId: string,
    date: string,
    status: "done" | "skipped",
): Promise<void> => {
    try {
        const dailyStatus = await getDailyStatus();

        if (!dailyStatus[habitId]) {
            dailyStatus[habitId] = {};
        }

        dailyStatus[habitId][date] = status;
        await saveDailyStatus(dailyStatus);
    } catch (error) {
        console.error("Error updating task status:", error);
        throw new Error("Failed to update task status");
    }
};

export const clearAllData = async (): Promise<void> => {
    try {
        await AsyncStorage.multiRemove([HABITS_KEY, DAILY_STATUS_KEY]);
        console.log("All data cleared successfully");
    } catch (error) {
        console.error("Error clearing data:", error);
        throw new Error("Failed to clear data");
    }
};